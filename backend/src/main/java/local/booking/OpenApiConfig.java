package local.booking;

import java.util.*;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.security.*;
import local.booking.booking.*;
import local.booking.space.SpaceController.SpaceView;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean OpenAPI bookingOpenApi() {
        return new OpenAPI().info(new Info().title("Booking local").version("1")
            .description("Basic en cada operación protegida. GET /api/csrf entrega token y headerName; "
                +"conserve la cookie de sesión del mismo navegador y envíe X-CSRF-TOKEN en POST/DELETE. "
                +"En Swagger: Authorize Basic, ejecutar GET /api/csrf, copiar token en Authorize csrfToken. "
                +"El token no sustituye Basic; no se guarda la autorización al recargar. "
                +"GET/PUT sobre /api/bookings/{id} no están implementados: 405 con Allow DELETE; ruta inexistente: 404."))
            .components(new Components()
                .addSecuritySchemes("basicAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"))
                .addSecuritySchemes("csrfToken",new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER).name("X-CSRF-TOKEN")
                    .description("Token de GET /api/csrf, ligado a la cookie de sesión del mismo navegador.")));
    }
    @Bean OpenApiCustomizer bookingContract() {
        return api -> {
            for(var type:List.of(BookingRequest.class,BookingResult.class,BookingView.class,SpaceView.class))
                ModelConverters.getInstance().read(type).forEach(api.getComponents()::addSchemas);
            api.getComponents().addSchemas("ApiProblem",new ObjectSchema()
                .addProperty("status",new IntegerSchema()).addProperty("title",new StringSchema())
                .addProperty("detail",new StringSchema()).addProperty("type",new StringSchema())
                .addProperty("instance",new StringSchema()).required(List.of("status","title")));
            for(var path:api.getPaths().entrySet()) for(var entry:path.getValue().readOperationsMap().entrySet()) {
                var operation=entry.getValue();var method=entry.getKey();
                boolean csrf=path.getKey().equals("/api/csrf");
                boolean mutation=method==PathItem.HttpMethod.POST||method==PathItem.HttpMethod.DELETE;
                var requirement=new SecurityRequirement().addList("basicAuth");
                if(mutation)requirement.addList("csrfToken"); // Same object means AND, never alternatives.
                operation.setSecurity(csrf?List.of():List.of(requirement));
                var responses=new ApiResponses();
                if(path.getKey().equals("/api/bookings")&&method==PathItem.HttpMethod.POST) {
                    responses.addApiResponse("201",response("Todas las ocurrencias creadas",ref("BookingResult")));
                    responses.addApiResponse("200",response("Aceptación parcial: creadas y rechazos específicos",ref("BookingResult")));
                    responses.addApiResponse("409",response("Ninguna ocurrencia disponible; created vacío",ref("BookingResult")));
                    problem(responses,"400","Campos, tokens numéricos o fechas inválidos");
                    problem(responses,"404","Espacio inexistente");
                    problem(responses,"415","Tipo de contenido no soportado");
                    if(operation.getRequestBody()!=null)operation.getRequestBody().setDescription(
                        "spaceId positivo y occurrences 1–12 deben ser tokens JSON enteros; 1.0, 1e0 y cadenas se rechazan. "
                        +"occurrences ausente/null equivale a 1. Fechas ISO8601 con offset, inicio anterior a fin.");
                } else if(method==PathItem.HttpMethod.DELETE) {
                    responses.addApiResponse("204",new ApiResponse().description("Propia cancelada; también si ya estaba cancelada"));
                    problem(responses,"400","Identificador mal formado");
                    problem(responses,"404","Reserva ajena o inexistente; respuesta indistinguible");
                } else {
                    Schema<?> schema;
                    if(path.getKey().equals("/api/bookings"))schema=new ArraySchema().items(ref("BookingView"));
                    else if(path.getKey().equals("/api/spaces"))schema=new ArraySchema().items(ref("SpaceView"));
                    else if(csrf)schema=new ObjectSchema().addProperty("token",new StringSchema())
                        .addProperty("headerName",new StringSchema().example("X-CSRF-TOKEN")).required(List.of("token","headerName"));
                    else schema=new ObjectSchema().addProperty("username",new StringSchema()).required(List.of("username"));
                    responses.addApiResponse("200",response(csrf?"Token CSRF enmascarado; conservar cookie de sesión":"Consulta autorizada",schema));
                }
                if(!csrf)problem(responses,"401","Identidad ausente o incorrecta");
                if(mutation)problem(responses,"403","Token CSRF ausente, inválido o de otra sesión");
                if(path.getKey().startsWith("/api/bookings")||path.getKey().equals("/api/spaces"))
                    problem(responses,"503","Acceso a datos no disponible; consultar antes de reintentar");
                problem(responses,"500","Error inesperado sin detalles internos");
                operation.setResponses(responses);
            }
        };
    }
    private static Schema<?> ref(String name){return new Schema<>().$ref("#/components/schemas/"+name);}
    private static ApiResponse response(String description,Schema<?> schema){
        return new ApiResponse().description(description).content(new Content().addMediaType("application/json",new MediaType().schema(schema)));
    }
    private static void problem(ApiResponses responses,String status,String description){
        responses.addApiResponse(status,new ApiResponse().description(description).content(new Content()
            .addMediaType("application/problem+json",new MediaType().schema(ref("ApiProblem")))));
    }
}
