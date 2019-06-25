var page = new WebPage(),
    address, size, loadDelay;

if (phantom.args.length !== 4) {
    console.log('Usage: rasterize.js URL width height load-delay');
    phantom.exit();
}

address = phantom.args[0];
loadDelay = phantom.args[3];

page.viewportSize = { width: parseInt(phantom.args[1]), height: phantom.args[2] };
page.clipRect = page.viewportSize;
page.onError = function() {};
page.open(address, function (status) {
    if (status !== 'success') {
        console.log('Unable to load the address!');
        phantom.exit(1);
    } else {
        window.setTimeout(function () {
            var base64 = page.renderBase64('PNG');
            console.log(base64);
            phantom.exit();
        }, loadDelay);
    }
});
