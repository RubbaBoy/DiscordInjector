/* The JS alone here comes from https://github.com/leovoel/BeautifulDiscord */

console.log("Injecting custom CSS stuff");

window._fs = require("fs");
window._path = require("path");
window._fileWatcher = null;
window._styleTag = {};

window.applyCSS = function (path, name) {
    var customCSS = window._fs.readFileSync(path, "utf-8");
    if (!window._styleTag.hasOwnProperty(name)) {
        window._styleTag[name] = document.createElement("style");
        document.head.appendChild(window._styleTag[name]);
    }
    window._styleTag[name].innerHTML = customCSS;
};

window.clearCSS = function (name) {
    if (window._styleTag.hasOwnProperty(name)) {
        window._styleTag[name].innerHTML = "";
        window._styleTag[name].parentElement.removeChild(window._styleTag[name]);
        delete window._styleTag[name];
    }
};

window.watchCSS = function (path) {
    if (window._fs.lstatSync(path).isDirectory()) {
        files = window._fs.readdirSync(path);
        dirname = path;
    } else {
        files = [window._path.basename(path)];
        dirname = window._path.dirname(path);
    }
    for (var i = 0; i < files.length; i++) {
        var file = files[i];
        if (file.endsWith(".css")) {
            window.applyCSS(window._path.join(dirname, file), file)
        }
    }
    if (window._fileWatcher === null) {
        window._fileWatcher = window._fs.watch(path, {encoding: "utf-8"},
            function (eventType, filename) {
                if (!filename.endsWith(".css")) return;
                path = window._path.join(dirname, filename);
                if (eventType === "rename" && !window._fs.existsSync(path)) {
                    window.clearCSS(filename);
                } else {
                    window.applyCSS(window._path.join(dirname, filename), filename);
                }
            }
        );
    }
};

window.tearDownCSS = function () {
    for (var key in window._styleTag) {
        if (window._styleTag.hasOwnProperty(key)) {
            window.clearCSS(key)
        }
    }
    if (window._fileWatcher !== null) {
        window._fileWatcher.close();
        window._fileWatcher = null;
    }
};
window.applyAndWatchCSS = function (path) {
    window.tearDownCSS();
    window.watchCSS(path);
};