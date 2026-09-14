-- Development catalog only, not an official SED inventory. No reservations are seeded.
-- Existing rows are preserved; reruns insert only missing IDs. IDs 1-99 are reserved for seeds.
MERGE INTO BKG_SPACE d
USING (
  SELECT 1 AS ID, 'Sala de reuniones' AS NAME, 'ROOM' AS SPACE_TYPE, 12 AS CAPACITY, 'Sede de desarrollo' AS SITE FROM dual
  UNION ALL
  SELECT 2, 'Auditorio', 'AUDITORIUM', 80, 'Sede de desarrollo' FROM dual
  UNION ALL
  SELECT 3, 'Zona colaborativa', 'COWORKING', 20, 'Sede de desarrollo' FROM dual
) s ON (d.ID = s.ID)
WHEN NOT MATCHED THEN INSERT (ID, NAME, SPACE_TYPE, CAPACITY, SITE)
VALUES (s.ID, s.NAME, s.SPACE_TYPE, s.CAPACITY, s.SITE);
