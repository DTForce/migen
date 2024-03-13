import com.dtforce.dokka.json.DokkaJsonResolver
import com.dtforce.migen.platform.MigenSqlBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaMethod

class TestResolver {
    @Test
    fun methodComment() {
        val read = DokkaJsonResolver.read("../build/dokka/html/index.json")
        Assertions.assertNotNull(read)
        val dokkaJsonFunction = DokkaJsonResolver.resolveMethod(read, MigenSqlBuilder::clone, MigenSqlBuilder::class)
        Assertions.assertNotNull(dokkaJsonFunction)
        Assertions.assertEquals("Clones the sql builder.", dokkaJsonFunction!!.documentation!!.asText)
    }

    @Test
    fun methodCommentJava() {
        val read = DokkaJsonResolver.read("../build/dokka/html/index.json")
        Assertions.assertNotNull(read)
        val dokkaJsonFunction = DokkaJsonResolver.resolveMethod(read, MigenSqlBuilder::clone.javaMethod!!, MigenSqlBuilder::class.java)
        Assertions.assertNotNull(dokkaJsonFunction)
        Assertions.assertEquals("Clones the sql builder.", dokkaJsonFunction!!.documentation!!.asText)
    }
}
