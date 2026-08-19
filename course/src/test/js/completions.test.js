const assert = require("node:assert/strict");

let completionCount = 0;

global.window = {
    callback: {
        completionSet: () => completionCount++
    }
};

global.XMLHttpRequest = class {
    listeners = {};

    open(_method, _url) {}

    send() {}

    addEventListener(name, listener) {
        this.listeners[name] = listener;
    }
};

require("../../main/assets/js_injection/completions.js");

function finish(url, status = 200, responseText = "") {
    const request = new XMLHttpRequest();
    request.open("POST", url);
    request.send();
    request.status = status;
    request.responseText = responseText;
    request.listeners.load.call(request);
}

finish("/handler/publish_completion");
finish("/handler/problem_check");
finish("/handler/do_attempt");
finish("/handler/render_grade", 200, "is--complete");
finish("/handler/render_grade", 200, "is--incomplete");
finish("/handler/problem_check", 403);
finish("/unrelated");

assert.equal(completionCount, 4);
