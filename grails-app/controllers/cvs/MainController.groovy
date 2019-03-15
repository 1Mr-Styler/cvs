package cvs

class MainController {

    MainService mainService

    def index() {

        render view: '/index'
    }

    def upload() {
        if (params.sig == null || params.check == null) {
            redirect action: 'index'
            return
        }

        File chequeFile = mainService.toFile(request.getFile("check"))
        String chequeFilename = chequeFile.name

        flash.hasUpload = true
        flash.image = chequeFilename

        //Run OCR
        String path = grailsApplication.config.fileLocation.toString()
        String ocr = mainService.ocr(chequeFilename, path)
        println(ocr)
        Map extract = mainService.extract(ocr)
        println(extract)
        double d = mainService.word2Num(extract.worded)

        flash.date = extract.date
        flash.amount = extract.amount
        flash.worded = extract.worded
        flash.compare = "${d} == ${extract.amount} ? ${d == extract.amount.toDouble()}"


        chain action: "index"
    }
}
