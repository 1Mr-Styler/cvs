<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title></title>
</head>

<body>
<div class="text-center py-5">
    <div class="container">
        <div class="row my-5 justify-content-center">
            <div class="col-md-9">
                <h1>Check Verification System</h1>
            <!--<p class="lead text-muted">...</p>-->

                <g:uploadForm controller="main" action="upload">
                    <p>Select Cheque Image</p>
                    <input type="file" class="btn btn-secondary m-2" name="check"/>

                    <p>Select Genuine Signatures</p>
                    <input type="file" class="btn btn-secondary m-2" name="sig" multiple="multiple"/>

                    <p></p>
                    <g:submitButton name="upload" value="Upload"/>
                </g:uploadForm>
            </div>
        </div>
    </div>
</div>
<g:if test="${flash.hasUpload != null}">
    <div class="py-4 bg-light">
        <div class="container">
            <div class="row">
                <div class="col-md-6 p-3">
                    <div class="card box-shadow">

                        <div class="list-group">
                            <a href="#" class="list-group-item list-group-item-action flex-column align-items-start">
                                <div class="d-flex w-100 justify-content-between">
                                    <h5 class="mb-1">Bank</h5>
                                    <small>Detected</small>
                                </div>

                                <p class="mb-1"><img class="card-img-top"
                                                     src="${assetPath(src: "${flash.wdir}/bank.png")}">
                                </p>
                                <div class="d-flex w-100 justify-content-between">
                                    <h5 class="mb-1">Signature</h5>
                                    <small>Detected</small>
                                </div>

                                <p class="mb-1"><img class="card-img-top"
                                                     src="${assetPath(src: "${flash.wdir}/signature.png")}">
                                </p>
                                <div class="d-flex w-100 justify-content-between">
                                    <h5 class="mb-1">Date</h5>
                                    <small>Detected</small>
                                </div>

                                <p class="mb-1"><img class="card-img-top"
                                                     src="${assetPath(src: "${flash.wdir}/date.png")}">
                                <div class="d-flex w-100 justify-content-between">
                                    <h5 class="mb-1">Legal Amount</h5>
                                    <small>Detected</small>
                                </div>

                                <p class="mb-1"><img class="card-img-top"
                                                     src="${assetPath(src: "${flash.wdir}/worded.png")}">
                                </p>
                            </a>
                        </div>
                    </div>
                </div>

                <div class="col-md-6>
                    <div class="card box-shadow">
                        <img class="card-img-top" src="${assetPath(src: "${flash.image}")}">
                        <table class="table table-dark">
                            <thead>
                            <tr>
                                <th scope="col">Sig. Score</th>
                                <th scope="col">Date</th>
                                <th scope="col">Worded Amount</th>
                                <th scope="col">Numeric Amount</th>
                                <th scope="col">Compare Amount</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th scope="row">N/A</th>
                                <td>${flash.date}</td>
                                <td>${flash.worded}</td>
                                <td>${flash.amount}</td>
                                <td>${flash.compare}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</g:if>
</body>
</html>
