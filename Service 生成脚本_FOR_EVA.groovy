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
    new File(dir, className + "Service.java").withPrintWriter { out -> generate(out, className, tableName, fields) }
    def implPath = "dir${File.separator}impl"
    new File(implPath).mkdirs()


}

def generate(out, className, tableName,fields) {
    out.println "package $packageName"
    out.println ""
    out.println "/**"
    out.println " * @author zhangyw"
    out.println " * @date " + new Date().toString()
    out.println " */"
    out.println ""
    out.println "public interface ${className}Service {"
    out.println ""
    generateCreate(out, className)
    deleteById(out,className)
    delete(out,className)
    deleteByIdInBatch(out,className)
    updateById(out,className)
    updateByIdInBatch(out,className)
    findById(out,className)
    findOne(out,className)
    findList(out,className)
    findPage(out,className)
    count(out,className)
    out.println ""
    out.println "}"
}

def generateCreate(out,className) {
    out.println '''
         /**
         * 创建
         *
         * @param odsEventSecure 实体对象
         * @return Long
         */
    '''
    out.println "   Long create($className ${instanceName(className)});"
}


def deleteById(out,className) {
    out.println '''
         /**
         * 主键删除
         *
         * @param id 主键
         */
    '''
    out.println "   void deleteById(Long id);"
}

def delete(out,className) {
    out.println '''
         /**
         * 删除
         *
         * @param odsEventSecure 实体对象
         */
    '''
    def insName = instanceName(className)
    out.println "   void delete($className $insName);"
}

def deleteByIdInBatch(out, className) {
    out.println '''
        /**
         * 批量主键删除
         *
         * @param ids 主键集
         */
    '''
    out.println "   void deleteByIdInBatch(List<Long> ids);"
}

def updateById(out, className) {
    def insName = instanceName(className)
    out.println '''
        /**
         * 主键更新
         *
         * @param ${insName} 实体对象
         */
    '''
    out.println "   void updateById($className $insName);"
}

def updateByIdInBatch( out, className) {
    out.println '''
        /**
         * 批量主键更新
         *
         * @param odsEventSecures 实体集
         */
    '''
    def insName = instanceName(className)
    out.println "   void updateByIdInBatch(List<$className> ${insName}s);"
}

def findById(out, className) {
    out.println '''
        /**
         * 主键查询
         *
         * @param id 主键
         * @return OdsEventSecure
         */
    '''
    out.println "   $className findById(Long id);"
}

def findOne( out, className) {
    out.println '''
        /**
         * 条件查询单条记录
         *
         * @param odsEventSecure 实体对象
         * @return OdsEventSecure
         */
    '''
    def insName = instanceName(className)
    out.println "   $className findOne($className $insName);"
}

def findList( out, className) {
    out.println '''
        /**
         * 条件查询
         *
         * @param odsEventSecure 实体对象
         * @return List<OdsEventSecure>
         */
    '''
    def insName = instanceName(className)
    out.println "   List<$className> findList($className $insName);"
}

def findPage( out, className) {
    out.println '''
        /**
         * 分页查询
         *
         * @param pageWrap 分页对象
         * @return PageData<OdsEventSecure>
         */
    '''
    out.println "   PageData<$className> findPage(PageWrap<$className> pageWrap);"
}

def count(out, className) {
    out.println '''
        /**
         * 条件统计
         *
         * @param odsEventSecure 实体对象
         * @return long
         */
    '''
    def insName = instanceName(className)
    out.println "   long count($className $insName);"
}


def instanceName(className) {
    def firstChart = (className+"").substring(0,1).toLowerCase()
    def lastString = (className+"").substring(1)
    return "${firstChart}${lastString}".toString()
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
