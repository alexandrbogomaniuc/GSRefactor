/**
 * Adds highlight to all modified text inputs on page and parent elements of checkboxes
 * For highlight to work on the page should be defined css class "changed"
 */

"use strict";

function inputChanged(e) {
    if (e.target.value == e.target.dataset.source) {
        e.target.classList.remove("changed");
    } else {
        e.target.classList.add("changed");
    }
}

function isTrue(value) {
    return value === 'true' || value === true;
}

function checkboxChanged(e) {
    if (isTrue(e.target.checked) == isTrue(e.target.dataset.source)) {
        e.target.parentElement.classList.remove("changed");
    } else {
        e.target.parentElement.classList.add("changed");
    }
}

function enableModifiedInputHighlight() {
    var inputs = document.body.getElementsByTagName("input");
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].type == "checkbox") {
            // Store original state in html5 "data-source" attribute
            inputs[i].dataset.source = inputs[i].checked;
            inputs[i].addEventListener("change", checkboxChanged);
        } else {
            inputs[i].dataset.source = inputs[i].value;
            inputs[i].addEventListener("input", inputChanged);
        }
    }
}

window.addEventListener("load", enableModifiedInputHighlight);
