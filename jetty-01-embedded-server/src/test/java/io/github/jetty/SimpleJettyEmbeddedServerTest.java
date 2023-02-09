package io.github.jetty;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Log4j2
@AllArgsConstructor(onConstructor_ = @Autowired)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest(properties = "app.jetty-port=${random.int(50000,65535)}")
class SimpleJettyEmbeddedServerTest {

    Environment environment;

    @Test
    @SneakyThrows
    void should_create_simple_server() {
        // given
        val port = environment.getProperty("app.jetty-port", Integer.class);
        log.info("port: {}", port);

        // when
        log.info("starting server...");
        val server = new Server(port);
        server.start();
        log.info("server {} started", server);

        // then
        log.info("stopping {} server...", server);
        server.stop();
        log.info("server {} stopped", server);
    }
}
