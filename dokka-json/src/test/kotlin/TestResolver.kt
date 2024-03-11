import com.dtforce.dokka.json.DokkaJsonResolver
import com.dtforce.migen.platform.MigenSqlBuilder
import org.junit.Assert
import org.junit.Test
import kotlin.reflect.jvm.javaMethod

class TestResolver {
    @Test
    fun methodComment() {
        val read = DokkaJsonResolver.read("../build/dokka/html/index.json")
        Assert.assertNotNull(read)
        val dokkaJsonFunction = DokkaJsonResolver.resolveMethod(read, MigenSqlBuilder::clone, MigenSqlBuilder::class)
        Assert.assertNotNull(dokkaJsonFunction)
        Assert.assertEquals("Clones the sql builder.", dokkaJsonFunction!!.documentation!!.asText)
    }

    @Test
    fun methodCommentJava() {
        val read = DokkaJsonResolver.read("../build/dokka/html/index.json")
        Assert.assertNotNull(read)
        val dokkaJsonFunction = DokkaJsonResolver.resolveMethod(read, MigenSqlBuilder::clone.javaMethod!!, MigenSqlBuilder::class.java)
        Assert.assertNotNull(dokkaJsonFunction)
        Assert.assertEquals("Clones the sql builder.", dokkaJsonFunction!!.documentation!!.asText)
    }
}
