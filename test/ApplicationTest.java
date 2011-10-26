import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void testThatLoginPageWorks() {
        Http.Response response = GET("/login");
	    assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
    
}