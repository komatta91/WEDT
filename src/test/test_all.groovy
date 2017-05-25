import com.opencsv.CSVReader
import groovy.io.FileType
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.support.GenericApplicationContext
import pl.edu.pw.elka.studia.wedt.service.CalculatorService

def ctx = new GenericApplicationContext()
new ClassPathBeanDefinitionScanner(ctx).scan('pl')
new XmlBeanDefinitionReader(ctx).loadBeanDefinitions('webapp/WEB-INF/appconfig-mvc.xml','webapp/WEB-INF/appconfig-root.xml')
ctx.refresh()
def calculatorService = ctx.getBean(CalculatorService.class)

new File('results.txt').withWriter { out ->
    out.println('## START ##')
    new File("resources/datasets/wordpairs").eachFileRecurse(FileType.FILES) { file ->
        CSVReader reader = new CSVReader(new FileReader(file), ':' as char)
        String[] nextLine
        out.println('## File: ' + file)
        out.flush()
        while ((nextLine = reader.readNext()) != null) {
            println('Starting calculation for ' + nextLine[0..2])
            def res = calculatorService.calculate("en", nextLine[0], nextLine[1])
            def resLine = nextLine[0..2] + res.googleDistance + res.angle + res.finalScore
            println(resLine)
            out.println(resLine.join(':'))
            out.flush()
        }
    }
    out.println('## END ##')
}

