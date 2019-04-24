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
        File chequeFile
        ArrayList<String> sigFileNames = new ArrayList<>()

        try {
            def sigs = request.getFiles("sig")
            chequeFile = mainService.toFile(request.getFile("check"))

            sigs.each { sig ->
                sigFileNames.add("/apps/home/grails-app/assets/images/" + mainService.toFile(sig).name)
            }

        } catch (e) {
            redirect action: 'index'
            return
        }

        String chequeFilename = chequeFile.name
        String wdir = mainService.processFile(chequeFilename)

        flash.hasUpload = true
        flash.image = "$wdir/main.png"
        flash.wdir = "$wdir"

        //Run OCR
        String path = grailsApplication.config.fileLocation.toString()
        String ocr = mainService.ocr(chequeFilename, path)
        println(ocr)
        Map extract = mainService.extract(ocr)
        println(extract)
        String worded = mainService.getWorded(wdir)
        double d = mainService.word2Num(worded)

        flash.confidence = mainService.sigrec(sigFileNames, "/apps/home/grails-app/assets/images/$wdir/signature.png")
        flash.date = extract.date
        flash.amount = extract.amount
        flash.worded = worded
        flash.compare = "${d} == ${extract.amount} ? ${d == extract.amount.replace(",", "").toDouble()}"


        chain action: "index"
    }
}
