package ar.edu.utn.frc.atenas.documentacion.client;

import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.commons.error.SolicitudInvalidaException;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.ArchivoDrive;
import ar.edu.utn.frc.atenas.documentacion.exception.ArchivoNoAccesibleException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpStatus;

class DriveClientImplTest {

    private DriveClientImpl crearCliente(MockRestServiceServer server) {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        return new DriveClientImpl(builder, "KEY");
    }

    @Test
    void extraerFileId_driveComun() {
        DriveClientImpl c = new DriveClientImpl(RestClient.builder(), "KEY");
        assertThat(c.extraerFileId("https://drive.google.com/file/d/ABC123/view")).isEqualTo("ABC123");
    }

    @Test
    void extraerFileId_docsGoogle() {
        DriveClientImpl c = new DriveClientImpl(RestClient.builder(), "KEY");
        assertThat(c.extraerFileId("https://docs.google.com/document/d/XYZ/edit")).isEqualTo("XYZ");
    }

    @Test
    void extraerFileId_openId() {
        DriveClientImpl c = new DriveClientImpl(RestClient.builder(), "KEY");
        assertThat(c.extraerFileId("https://drive.google.com/open?id=QWE")).isEqualTo("QWE");
    }

    @Test
    void extraerFileId_malFormado() {
        DriveClientImpl c = new DriveClientImpl(RestClient.builder(), "KEY");
        assertThatThrownBy(() -> c.extraerFileId("https://example.com/foo"))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void obtenerArchivo_200Pdf() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DriveClientImpl c = new DriveClientImpl(builder, "KEY");
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/files/FILE1")))
                .andRespond(withSuccess("{\"id\":\"FILE1\",\"name\":\"a.pdf\",\"mimeType\":\"application/pdf\",\"modifiedTime\":\"2025-01-01T00:00:00Z\"}", MediaType.APPLICATION_JSON));
        ArchivoDrive a = c.obtenerArchivo("FILE1");
        assertThat(a.fileId()).isEqualTo("FILE1");
        assertThat(a.mimeType()).isEqualTo("application/pdf");
        server.verify();
    }

    @Test
    void obtenerArchivo_404() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DriveClientImpl c = new DriveClientImpl(builder, "KEY");
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/files/FILE2")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("{\"error\":{\"code\":404}}").contentType(MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> c.obtenerArchivo("FILE2"))
                .isInstanceOf(ArchivoNoAccesibleException.class);
        server.verify();
    }

    @Test
    void obtenerArchivo_403Permisos() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DriveClientImpl c = new DriveClientImpl(builder, "KEY");
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/files/FILE3")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).body("{\"error\":{\"message\":\"File not found or permission denied\"}}").contentType(MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> c.obtenerArchivo("FILE3"))
                .isInstanceOf(ArchivoNoAccesibleException.class);
        server.verify();
    }

    @Test
    void obtenerArchivo_403Cuota() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DriveClientImpl c = new DriveClientImpl(builder, "KEY");
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/files/FILE4")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).body("{\"error\":{\"message\":\"Quota exceeded\"}}").contentType(MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> c.obtenerArchivo("FILE4"))
                .isInstanceOf(ServicioNoDisponibleException.class);
        server.verify();
    }

    @Test
    void obtenerArchivo_5xx() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DriveClientImpl c = new DriveClientImpl(builder, "KEY");
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/files/FILE5")))
                .andRespond(withServerError());
        assertThatThrownBy(() -> c.obtenerArchivo("FILE5"))
                .isInstanceOf(ServicioNoDisponibleException.class);
        server.verify();
    }
}