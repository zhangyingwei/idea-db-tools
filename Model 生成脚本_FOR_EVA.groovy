import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "com.sample;"
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
    new File(dir, className + ".java").withPrintWriter { out -> generate(out, className, tableName, fields) }
}

def generate(out, className, tableName,fields) {
    out.println "package $packageName"
    out.println "import io.swagger.annotations.ApiModel;"
    out.println "import lombok.Data;"
    out.println "import com.baomidou.mybatisplus.annotation.TableName;"
    out.println "import com.baomidou.mybatisplus.annotation.TableId;"
    out.println "import io.swagger.annotations.ApiModelProperty;"
    out.println "import com.baomidou.mybatisplus.annotation.IdType;"
    out.println ""
    out.println ""
    out.println "/**"
    out.println " *"
    out.println " * @author zhangyw"
    out.println " * @date " + new Date().toString()
    out.println " */"
    out.println ""
    out.println "@Data"
    out.println "@ApiModel(\"\")"
    out.println "@TableName(\"$tableName\")"
    out.println "public class $className {"
    out.println ""
    fields.each() {
        if (it.annos != "") out.println "  ${it.annos}"
        if (it.name == "id") out.println "  @TableId(type = IdType.AUTO)"
        out.println "  @ApiModelProperty(value = \"${it.comment}\")"
        if (it.type == "Date") out.println "  @JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")"
        out.println "  private ${it.type} ${it.name};"
    }
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
