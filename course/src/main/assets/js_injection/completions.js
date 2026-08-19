// Injection to intercept completion state for xBlocks in the main document and child frames.
(function() {
    if (window.openEdxCompletionListenerInstalled) return;
    window.openEdxCompletionListenerInstalled = true;

    const originalOpen = XMLHttpRequest.prototype.open;
    const originalSend = XMLHttpRequest.prototype.send;

    XMLHttpRequest.prototype.open = function() {
        this.openEdxRequestUrl = arguments[1] || "";
        return originalOpen.apply(this, arguments);
    };

    XMLHttpRequest.prototype.send = function() {
        this.addEventListener("load", function() {
            const url = this.openEdxRequestUrl;
            const response = this.responseText || "";
            const completionUpdated = this.status === 200 && (
                url.includes("publish_completion") ||
                url.includes("problem_check") ||
                url.includes("do_attempt") ||
                (url.includes("render_grade") && response.includes("is--complete"))
            );

            if (completionUpdated) window.callback.completionSet();
        });
        return originalSend.apply(this, arguments);
    };
})();
