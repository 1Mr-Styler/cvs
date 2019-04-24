package cvs

import grails.gorm.transactions.Transactional

@Transactional
class MainService {

    File toFile(def stuff) {
        File convFile = new File("/apps/home/grails-app/assets/images/" + stuff.getOriginalFilename().replace(" ", ""));
//        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + stuff.getOriginalFilename());
        stuff.transferTo(convFile);

        convFile
    }

    String processFile(String filename) {
        String cmd = "python2.7 /apps/home/src/main/misc/roi.pyc /apps/home/grails-app/assets/images/ /apps/home/grails-app/assets/images/$filename"
        def proc = cmd.execute()
        proc.waitFor()

        String wdir = proc.in.getText('UTF-8').replace("\n", "")

        cmd = "python2.7 /apps/home/src/main/misc/jn.pyc /apps/home/grails-app/assets/images/$wdir/ date"
        def jn_date = cmd.execute()
        jn_date.waitFor()
        boolean hasDate = false
        String jnDate = jn_date.in.getText('UTF-8')
        if (!jnDate.contains("FNF")) {
            hasDate = true
        }

        cmd = "python2.7 /apps/home/src/main/misc/jn.pyc /apps/home/grails-app/assets/images/$wdir/ worded"
        def jn_worded = cmd.execute()
        jn_worded.waitFor()
        boolean hasWorded = false
        String jnWorded = jn_worded.in.getText('UTF-8')
        if (!jnWorded.contains("FNF")) {
            hasWorded = true
        }

        println("Has Date: ${jnDate} - $hasDate")
        println("Has Worded: ${jnWorded} - $hasWorded")
        wdir
    }

    String ocr(String file, String path) {
        StringBuilder pred = new StringBuilder()
        String filepath = "/apps/home/grails-app/assets/images/"

        try {
            String cmd = "python2.7 ${path}ocr.pyc $filepath${file} checkpoint/model.ckpt-92900.data-00000-of-00001"
            def proc = cmd.execute()
            proc.waitFor()
            def ypred = proc.in.getText('UTF-8').split("\n")
            ypred[0] = null

            for (String line : ypred) {
                pred.append(line)
                pred.append("\n")
            }

            println(pred)

            return pred.toString()

        } catch (Exception e) {
            throw new Exception(e)
        }
    }

    String ocr2(String filename, String wdir) {
        StringBuilder pred = new StringBuilder()
        String filepath = "/apps/home/grails-app/assets/images/"

        try {
            String cmd = "python2.7 /apps/home/src/main/misc/ocr.pyc $filepath$wdir/${filename} checkpoint/model.ckpt-92900.data-00000-of-00001"
            def proc = cmd.execute()
            proc.waitFor()
            def ypred = proc.in.getText('UTF-8').split("\n")
            ypred[0] = "null"

            for (String line : ypred) {
                pred.append(line)
                pred.append("\n")
            }

            println(pred)

            return pred.toString()

        } catch (Exception e) {
            throw new Exception(e)
        }
    }

    String getWorded(String wdir) {
        String text = ocr2("worded.png", wdir)

        text = text.replace("\n", "")
                .toUpperCase()
                .replace("NULL", "")
                .replace("RINGGIT", "")
                .replace("MALAYSIA", "").replace(" ", "")

        String cmd = "python2.7 /apps/home/src/main/misc/plode.pyc $text"
        def proc = cmd.execute()
        proc.waitFor()
        String exp = proc.in.getText('UTF-8').replace("\n", "").toUpperCase()

        exp
    }

    HashMap extract(String text) {
        HashMap<String, String> list = new HashMap<>()

        String date
        try {
            date = (text.replace("\n", "") =~ /(\d{2}\/\d{2}\/\d{4})/)[0][1]
        } catch (ignored) {
            try {
                date = (text.replace("\n", "") =~ /(\d{2}-\d{2}-\d{4})/)[0][1]
            } catch (e) {
                try {
                    date = (text.replace("\n", "") =~ /([\d]{6})/)[0][1]
                    date = date.take(2) + "/" + date.drop(2).take(2) + "/" + date.drop(4)
                } catch (ok) {
                    date = "Unsupported Date Format"
                }
            }
        }


        String amount
        try {
            amount = (text =~ /RM\s?([0-9,\.]+)/)[0][1]
        } catch (ignored) {
            amount = "0.0"
        }

        list.put("date", date)
        list.put("amount", amount)
        list.put("worded", tryGettingWordedAmount(text))

        list
    }

    String tryGettingWordedAmount(String text) {
        ArrayList<String> words = new ArrayList<String>(["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "twenty", "thirty", "forty"])
        text = text.toLowerCase()

        String wordsN = ""
        words.each { word ->
            try {
                String s = text.replace("\n", " ").substring(text.indexOf(word) + word.length(), text.indexOf("only"))
                if (s.length() < 40) {
                    wordsN = "$word $s only"
                }
            } catch (ignored) {
                println("$word - ${ignored.getMessage()}")
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

        word = word.toLowerCase()
        word = word.substring(0, word.indexOf("cents"))
        if (word.endsWith("and "))
            word = word.substring(0, word.indexOf(" and "))

/*        word.split(" ").each { w ->
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

        *//* //Process thousands
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

        boolean isValidInput = true;
        long result = 0;
        long finalResult = 0;
        List<String> allowedStrings = Arrays.asList(
                "zero", "one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen",
                "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty",
                "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety",
                "hundred", "thousand", "million", "billion", "trillion"
        );

        String input = word

        if (input != null && input.length() > 0) {
            input = input.replaceAll("-", " ");
            input = input.toLowerCase().replaceAll(" and", " ");
            String[] splittedParts = input.trim().split("\\s+");

            for (String str : splittedParts) {
                if (!allowedStrings.contains(str)) {
                    isValidInput = false;
                    System.out.println("Invalid word found : " + str);
                    break;
                }
            }
            if (isValidInput) {
                for (String str : splittedParts) {
                    if (str.equalsIgnoreCase("zero")) {
                        result += 0;
                    } else if (str.equalsIgnoreCase("one")) {
                        result += 1;
                    } else if (str.equalsIgnoreCase("two")) {
                        result += 2;
                    } else if (str.equalsIgnoreCase("three")) {
                        result += 3;
                    } else if (str.equalsIgnoreCase("four")) {
                        result += 4;
                    } else if (str.equalsIgnoreCase("five")) {
                        result += 5;
                    } else if (str.equalsIgnoreCase("six")) {
                        result += 6;
                    } else if (str.equalsIgnoreCase("seven")) {
                        result += 7;
                    } else if (str.equalsIgnoreCase("eight")) {
                        result += 8;
                    } else if (str.equalsIgnoreCase("nine")) {
                        result += 9;
                    } else if (str.equalsIgnoreCase("ten")) {
                        result += 10;
                    } else if (str.equalsIgnoreCase("eleven")) {
                        result += 11;
                    } else if (str.equalsIgnoreCase("twelve")) {
                        result += 12;
                    } else if (str.equalsIgnoreCase("thirteen")) {
                        result += 13;
                    } else if (str.equalsIgnoreCase("fourteen")) {
                        result += 14;
                    } else if (str.equalsIgnoreCase("fifteen")) {
                        result += 15;
                    } else if (str.equalsIgnoreCase("sixteen")) {
                        result += 16;
                    } else if (str.equalsIgnoreCase("seventeen")) {
                        result += 17;
                    } else if (str.equalsIgnoreCase("eighteen")) {
                        result += 18;
                    } else if (str.equalsIgnoreCase("nineteen")) {
                        result += 19;
                    } else if (str.equalsIgnoreCase("twenty")) {
                        result += 20;
                    } else if (str.equalsIgnoreCase("thirty")) {
                        result += 30;
                    } else if (str.equalsIgnoreCase("forty")) {
                        result += 40;
                    } else if (str.equalsIgnoreCase("fifty")) {
                        result += 50;
                    } else if (str.equalsIgnoreCase("sixty")) {
                        result += 60;
                    } else if (str.equalsIgnoreCase("seventy")) {
                        result += 70;
                    } else if (str.equalsIgnoreCase("eighty")) {
                        result += 80;
                    } else if (str.equalsIgnoreCase("ninety")) {
                        result += 90;
                    } else if (str.equalsIgnoreCase("hundred")) {
                        result *= 100;
                    } else if (str.equalsIgnoreCase("thousand")) {
                        result *= 1000;
                        finalResult += result;
                        result = 0;
                    } else if (str.equalsIgnoreCase("million")) {
                        result *= 1000000;
                        finalResult += result;
                        result = 0;
                    } else if (str.equalsIgnoreCase("billion")) {
                        result *= 1000000000;
                        finalResult += result;
                        result = 0;
                    } else if (str.equalsIgnoreCase("trillion")) {
                        result *= 1000000000000L;
                        finalResult += result;
                        result = 0;
                    }
                }

                finalResult += result;
                result = 0;
            }
        }
        rc = finalResult
        println(rc)

        rc.doubleValue()
    }

    Double sigrec(ArrayList<String> sigs, String qF) {

        String cmd = "python2.7 /apps/home/src/main/misc/sigrec.pyc "
        sigs.each { sig ->
            cmd += sig + " "
        }
        cmd += qF
        def proc = cmd.execute()
        def sout = new StringBuilder(), serr = new StringBuilder()
        proc.consumeProcessOutput(sout, serr)
        proc.waitFor()
        def data = sout.toString()//proc.in.getText('UTF-8')


        println(data.trim().split("\n"))

        data.trim().split("\n")[-1]?.take(5)?.toDouble() ?: 0.0
    }

}
