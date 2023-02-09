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
class JettyServletRestApiTest {

    Environment environment;

    public static class HelloServlet extends HttpServlet {
        @Override
        @SneakyThrows
        protected void service(HttpServletRequest request, HttpServletResponse response) {
            log.info("handling HTTP Servlet service method...");
            response.setStatus(HttpServletResponse.SC_OK);
            // response.setContentType("application/json");
            // response.setContentType("text/html");
            @Cleanup val writer = response.getWriter();
            writer.append("\"Hello!\"");
            writer.flush();
        }
    }

    public interface HelloClient {
        @POST("/api/hello")
        Call<String> greet();
    }

    @Test
    @SneakyThrows
    void should_call_rest_api() {
        // given
        val server = new Server();
        val serverConnector = new ServerConnector(server);

        // and
        val port = environment.getProperty("app.jetty-port", Integer.class);
        serverConnector.setPort(port);
        server.setConnectors(new ServerConnector[] { serverConnector });

        // and
        val context = new ServletContextHandler();
        context.setContextPath("/api");
        context.addServlet(HelloServlet.class, "/hello");

        // and
        val handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{ context, new DefaultHandler() });
        server.setHandler(handlers);

        log.info("starting server...");
        server.start();
        log.info("server {} started", server);

        // when
        val retrofit = new Retrofit.Builder().baseUrl(String.format("http://127.0.0.1:%d", port))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        val helloClient = retrofit.create(HelloClient.class);
        val response = helloClient.greet();

        // then
        val greeting = response.execute();
        log.info("response: {}", greeting);
        log.info("greeting message: {}", greeting.body());
        assertThat(greeting.body()).containsIgnoringCase("hello");
        assertThat(greeting.code()).isEqualTo(200);

        // tear down
        log.info("stopping {} server...", server);
        server.stop();
        log.info("server {} stopped", server);
    }
}
