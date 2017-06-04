import com.opencsv.CSVReader
import groovy.io.FileType
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.support.GenericApplicationContext
import pl.edu.pw.elka.studia.wedt.service.CalculatorService
import pl.edu.pw.elka.studia.wedt.service.WikiService

println("Creating spring context...")
def ctx = new GenericApplicationContext()
new ClassPathBeanDefinitionScanner(ctx).scan('pl')
new XmlBeanDefinitionReader(ctx).loadBeanDefinitions('webapp/WEB-INF/appconfig-mvc.xml','webapp/WEB-INF/appconfig-root.xml')
ctx.refresh()
println("Creating beans...")
def calculatorService = ctx.getBean(CalculatorService.class)
def wikiService = ctx.getBean(WikiService.class)
def forbidden = wikiService.getForbiddenArticleTitles("en")
println("Starting tests.")

new File('results.txt').withWriter { outFine ->
    new File('results_err.txt').withWriter { outErrored ->
        outFine.println('## START ##')
        outErrored.println('## START ##')
        new File("resources/datasets/wordpairs").eachFileRecurse(FileType.FILES) { file ->
            CSVReader reader = new CSVReader(new FileReader(file), ':' as char)
            String[] nextLine
            outFine.println('## File: ' + file)
            outErrored.println('## File: ' + file)
            outFine.flush()
            outErrored.flush()
            while ((nextLine = reader.readNext()) != null) {
                nextLine[2] = new BigDecimal(10).subtract(new BigDecimal(nextLine[2])).divide(10, 20, BigDecimal.ROUND_HALF_EVEN).toString()
                println('Starting calculation for ' + nextLine[0..2])
                if(forbidden.contains(nextLine[0]) || forbidden.contains(nextLine[1])){
                    def resLine = nextLine[0..2] + "FORBIDDEN"
                    println(resLine)
                    outErrored.println(resLine.join(':'))
                    outErrored.flush()
                } else {
                    try{
                        def res = calculatorService.calculate("en", nextLine[0], nextLine[1])
                        def resLine = nextLine[0..2] + res.googleDistance + res.googleTime + res.angle + res.angleTime + res.finalScore + res.totalTime
                        println(resLine)
                        outFine.println(resLine.join(':'))
                        outFine.flush()
                    }catch(Exception e){
                        def resLine = nextLine[0..2] + e.getMessage()
                        println(resLine)
                        outErrored.println(resLine.join(':'))
                        outErrored.flush()
                    }
                }
            }
        }
        outFine.println('## END ##')
        outErrored.println('## END ##')
    }
}

