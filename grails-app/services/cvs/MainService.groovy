package cvs

import grails.gorm.transactions.Transactional
import org.springframework.util.StringUtils

@Transactional
class MainService {

    File toFile(def stuff) {
        File convFile = new File("/Users/styl3r/IdeaProjects/CVS/cvs/grails-app/assets/images/" + stuff.getOriginalFilename());
//        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + stuff.getOriginalFilename());
        stuff.transferTo(convFile);

        convFile
    }

    String ocr(String file, String path) {
        StringBuilder pred = new StringBuilder()
        String filepath = "/Users/styl3r/IdeaProjects/CVS/cvs/grails-app/assets/images/"

        try {
            def proc = "python ${path}ocr.pyc $filepath${file} checkpoint/model.ckpt-92900.data-00000-of-00001".execute()
            def ypred = proc.in.getText('UTF-8').split("\n")

            for (String line : ypred) {
                pred.append(line)
                pred.append("\n")
            }

            return pred.toString()

        } catch (Exception e) {
            throw new Exception(e)
        }
    }

    HashMap extract(String text) {
        HashMap<String, String> list = new HashMap<>()
        String date = (text =~ /(\d{2}\/\d{2}\/\d{4})/)[0][1]
        String amount = (text =~ /RM\s?([0-9,\.]+)/)[0][1]

        list.put("date", date)
        list.put("amount", amount)
        list.put("worded", tryGettingWordedAmount(text))

        list
    }

    String tryGettingWordedAmount(String text) {
        ArrayList<String> words = new ArrayList<String>(["one", "two", "three", "four", "five", "six", "seven", "eight", "nine"])
        text = text.toLowerCase()

        String wordsN = ""
        words.each { word ->
            String s = text.substring(text.indexOf(word) + word.length(), text.indexOf("only"))
            if (s.length() < 40) {
                wordsN = "$word $s only"
            }
        }

        wordsN
    }

    Double word2Num(String word) {
        HashMap<String, String> map = new HashMap<>(
                "one": "1", "two": "2", "three": "3", "four": "4",
                "five": "5", "six": "6", "seven": "7", "eight": "8",
                "nine": "9", "ten": "10", "eleven": "11", "twelve": "12",
                "thirteen": "13", "fourteen": "14", "fifteen": "15", "sixteen": "16",
                "seventeen": "17", "eighteen": "18", "nineteen": "19",
                "twenty": "20", "thirty": "30", "forty": "40",
                "fifty": "50", "sixty": "60", "seventy": "70", "eighty": "80",
                "ninety": "90",
        )

        int rc = 0
        word.split(" ").each { w ->
            if (w.length() > 0) {
                if (map.containsKey(w)) {
                    rc += map[w].toInteger()
                } else if (w == "thousand") {
                    rc *= 1000
                } else if (w == "hundred") {
                    rc *= 100
                }
            }
        }

        /* //Process thousands
         if (word.contains("ty") && !rc.contains(",")) {
             rc = rc.replaceFirst("0", "")

             if (word.contains("thousand") && !word.endsWith("thousand")) {
                 String s = word.split("thousand")
 //                rc = map
             }
         }

         //Process ands
         if (rc.contains(",")) {
             def r = rc.split(",")
             int g = StringUtils.countOccurrencesOf(r[0], "0")
             switch (g) {
                 case 2:
                     rc = "${r[0].take(1)}${r[1]}"
             }
         }*/
        println(rc)

        rc.doubleValue()
    }

}
