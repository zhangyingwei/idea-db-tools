import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "com.sample.mapper;"
typeMapping = [
        (~/(?i)int/)                      : "long",
        (~/(?i)float|double|decimal|real/): "double",
        (~/(?i)datetime|timestamp/)       : "java.util.Date",
        (~/(?i)date/)                     : "java.util.Date",
        (~/(?i)time/)                     : "java.util.Date",
        (~/(?i)/)                         : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = javaName(table.getName(), true)
    def tableName = table.getName()
    def fields = calcFields(table)
    new File(dir, className + "Mapper.java").withPrintWriter { out -> generate(out, className, tableName, fields) }
}

def generate(out, className, tableName, fields) {
    def mapperName = className+"Mapper"
    def createDateTime = new Date().toString()
    out.println "package $packageName"
    out.println "import com.baomidou.mybatisplus.core.mapper.BaseMapper;"
    out.println ""
    out.println "/**"
    out.println " *"
    out.println " * @author zhangyw"
    out.println " * @date $createDateTime"
    out.println " */"
    out.println ""
    out.println "public interface $mapperName extends BaseMapper<$className> {"
    out.println ""
    out.println "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        def comment = col.comment.toString().replaceAll("\\n","\\t")
        fields += [[
                           name : javaName(col.getName(), false),
                           type : typeStr,
                           annos: "",
                            comment: comment
                   ]]
    }
}

def javaName(str, capitalize) {
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
            .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}

