package local.booking.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.sql.DataSource;
import local.booking.space.Space;
import local.booking.space.SpaceRepository;
import local.booking.space.SpaceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OraclePersistenceTest {
    @Autowired EntityManager entityManager;
    @Autowired BookingRepository bookings;
    @Autowired SpaceRepository spaces;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;

    @Test
    void connectsAsBookingInFreepdb1WithValidatedMapping() {
        assertThat(jdbc.queryForObject("SELECT USER FROM dual", String.class)).isEqualTo("BOOKING");
        assertThat(jdbc.queryForObject("SELECT SYS_CONTEXT('USERENV','CON_NAME') FROM dual", String.class))
                .isEqualTo("FREEPDB1");
        assertThat(spaces.findById(1L)).isPresent();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM BKG_SPACE WHERE ID IN (1,2,3)", Integer.class))
                .isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2030-01-15T10:00:00.123456789-05:00", "2030-01-15T17:00:00.987654321+02:00", "2030-01-15T15:00:00Z"})
    void persistsExactInstantAndRelationship(String input) {
        OffsetDateTime start = OffsetDateTime.parse(input);
        Booking saved = bookings.saveAndFlush(new Booking(spaces.getReferenceById(1L),
                "t003-" + UUID.randomUUID(), start, start.plusHours(1), BookingStatus.ACTIVE));
        Long id = saved.getId();
        entityManager.clear();
        Booking loaded = bookings.findById(id).orElseThrow();
        assertThat(loaded.getStartsAt().toInstant()).isEqualTo(start.toInstant());
        assertThat(loaded.getEndsAt().toInstant()).isEqualTo(start.plusHours(1).toInstant());
        assertThat(loaded.getSpace().getId()).isEqualTo(1L);
        assertThat(loaded.getSpace().getType()).isEqualTo(SpaceType.ROOM);
        assertThat(loaded.getOwnerId()).startsWith("t003-");
        assertThat(loaded.getStatus()).isEqualTo(BookingStatus.ACTIVE);
    }

    @Test
    void spaceSequenceDoesNotCollideWithSeedIds() {
        Space saved = spaces.saveAndFlush(new Space("Test temporal", SpaceType.ROOM, 2, "Prueba"));
        assertThat(saved.getId()).isGreaterThanOrEqualTo(100L);
        entityManager.clear();
        assertThat(spaces.findById(saved.getId())).isPresent();
    }

    @Test
    void repeatedSeedsPreserveRowsAndExistingEdits() {
        Long count = spaces.count();
        jdbc.update("UPDATE BKG_SPACE SET NAME = ? WHERE ID = 1", "Cambio solo en transacción de prueba");
        ResourceDatabasePopulator seed = new ResourceDatabasePopulator(
                new ClassPathResource("db/oracle/R__development_spaces.sql"));
        seed.execute(dataSource);
        seed.execute(dataSource);
        assertThat(spaces.count()).isEqualTo(count);
        assertThat(jdbc.queryForObject("SELECT NAME FROM BKG_SPACE WHERE ID=1", String.class))
                .isEqualTo("Cambio solo en transacción de prueba");
    }

    @Test
    void rejectsMissingSpace() {
        assertOracleError(2291, () -> insertBooking(-999L, "owner", "ACTIVE",
                "2030-01-15T10:00:00-05:00", "2030-01-15T11:00:00-05:00"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2030-01-15T10:00:00-05:00", "2030-01-15T09:00:00-05:00"})
    void rejectsZeroOrNegativeRange(String end) {
        assertOracleError(2290, () -> insertBooking(1L, "owner", "ACTIVE",
                "2030-01-15T10:00:00-05:00", end));
    }

    @Test
    void rejectsUnknownStatus() {
        assertOracleError(2290, () -> insertBooking(1L, "owner", "UNKNOWN",
                "2030-01-15T10:00:00-05:00", "2030-01-15T11:00:00-05:00"));
    }

    @Test
    void requiresOwner() {
        assertOracleError(1400, () -> insertBooking(1L, null, "ACTIVE",
                "2030-01-15T10:00:00-05:00", "2030-01-15T11:00:00-05:00"));
    }

    @Test
    void rejectsInvalidSpaceTypeAndCapacity() {
        assertOracleError(2290, () -> jdbc.update("INSERT INTO BKG_SPACE VALUES (BKG_SPACE_SEQ.NEXTVAL, ?, ?, ?, ?)",
                "Prueba", "ROOM", 0, "Prueba"));
        assertOracleError(2290, () -> jdbc.update("INSERT INTO BKG_SPACE VALUES (BKG_SPACE_SEQ.NEXTVAL, ?, ?, ?, ?)",
                "Prueba", "UNKNOWN", 2, "Prueba"));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"9,11,true","11,13,true","10,12,true","9,13,true","10,11,true","11,12,true","8,10,false","12,14,false","6,8,false","14,16,false"})
    void oracleCollisionQueryCoversEveryBoundary(int startHour,int endHour,boolean expected) {
        var start=OffsetDateTime.parse("2037-01-01T10:00:00-05:00");
        bookings.saveAndFlush(new Booking(spaces.getReferenceById(1L),"boundary-test",start,start.plusHours(2),BookingStatus.ACTIVE));
        assertThat(bookings.collisions(1L,start.withHour(startHour),start.withHour(endHour))>0).isEqualTo(expected);
    }

    @Test void cancelledAndOtherSpaceDoNotBlockAnInterval() {
        var start=OffsetDateTime.parse("2037-01-01T10:00:00-05:00");
        var row=bookings.saveAndFlush(new Booking(spaces.getReferenceById(1L),"boundary-test",start,start.plusHours(2),BookingStatus.ACTIVE));
        assertThat(bookings.collisions(2L,start,start.plusHours(1))).isZero();
        row.cancel();bookings.flush();
        assertThat(bookings.collisions(1L,start,start.plusHours(1))).isZero();
    }

    private void insertBooking(Long spaceId, String owner, String status, String start, String end) {
        jdbc.update("INSERT INTO BKG_BOOKING (ID, SPACE_ID, OWNER_ID, STARTS_AT, ENDS_AT, STATUS) "
                + "VALUES (BKG_BOOKING_SEQ.NEXTVAL, ?, ?, ?, ?, ?)", spaceId, owner,
                OffsetDateTime.parse(start), OffsetDateTime.parse(end), status);
    }

    private void assertOracleError(int code, Runnable statement) {
        assertThatThrownBy(statement::run).isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(error -> {
                    Throwable cause = error;
                    while (!(cause instanceof SQLException) && cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    assertThat(cause).isInstanceOf(SQLException.class);
                    assertThat(((SQLException) cause).getErrorCode()).isEqualTo(code);
                });
    }
}
