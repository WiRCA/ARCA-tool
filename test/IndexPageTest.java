import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

public class IndexPageTest extends FunctionalTest {

    @Test
    public void mainPageWorksTest() {
        Http.Response response = GET("/");
	    assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
    
}