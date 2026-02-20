const DEFAULT_SHOWED_GAMES_COUNT = 8;
let showedGamesCount = DEFAULT_SHOWED_GAMES_COUNT;

document.addEventListener("DOMContentLoaded", checkLang);

function checkLang() {
    if (document.location.href.includes("lang=zh-cn")) {
        let span = document.querySelector('.faux-select .selected-option span');
        span.textContent = 'Language: CHINESE';
    }
    showGames();
}

function showGames() {
    let allGames = document.querySelectorAll("#list ul.active li");
    if (allGames.length > 0) {
        if (showedGamesCount > allGames.length - 1) {
            showedGamesCount = allGames.length;
        }
        for (let i = 0; i < showedGamesCount; i++) {
            let game = allGames[i];
            let classList = game.classList;
            classList.add("active");
        }
    }

    if (document.querySelectorAll("#list ul.active li.active").length === allGames.length) {
        document.getElementById("load-more-games").style.display = "none";
    } else {
        document.getElementById("load-more-games").style.removeProperty("display");
    }
}

function loadMoreGames() {
    showedGamesCount += DEFAULT_SHOWED_GAMES_COUNT;
    showGames();
}


$(function () {
    //Game Tabs
    $("#nav li a").click(function () {
        showedGamesCount = DEFAULT_SHOWED_GAMES_COUNT;
        var curID = $(this).parent().attr("id");

        $("#nav li").removeClass("active");
        $(this).parent().addClass("active");

        $("#list ul").removeClass("active");
        $("#list ul#" + curID + "_list").addClass("active");

        $("#list ul li").removeClass("active");
        showGames();

        return false;
    });

    const swiper = new Swiper('#headerIn', {
        loop: true,

        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },

        autoplay: {
            delay: 5000,
        },
    });
});

$("#search-games").on('keyup paste drop', function () {
    $("#result-games").html("");
    var searchField = $("#search-games").val();
    if (searchField.length < 3) return;
    var expression = new RegExp(searchField, "i");
    var output = '';
    var data = JSON.parse(window.GAME_LIST);

    if (searchField) {
        $.each(data, function (key, value) {
            if (typeof value.title !== 'undefined' && value.title.search(expression) !== -1) {
                if ($(window).width() < 768) {
                    output += '<a class="mobile" href="' + window.OPEN_GAME_LINK + value.id +
                        '" target="_blank"><li class="list-group-item">' + value.title + '</li></a>';
                } else {
                    output += '<a class="desktop" href="javascript:void(0)" onclick="changePlayDemoSrc(this)" data-video="'
                        + window.OPEN_GAME_LINK + value.id + '"><li class="list-group-item">' + value.title + '</li></a>';
                }
            }
        });
        $('#result-games').html(output);
    } else {
        $("#result-games").html("");
    }
});

$(document).click(function (event) {
    // if (!($("#result-games").is(":hover")) && !($("#search-games").is(":hover")) && !($("#game-search").is(":hover"))) {
    //   $("#result-games").hide();
    // }
    if ($("#demo-overlay").hasClass("table-me")) {
        if ($(event.target).hasClass("demo-overlay-inner")) {
            emptyPlayDemoSrc();
        }
    }
    if (($("#result-games").is(":hover")) || ($("#search-games").is(":hover")) || ($("#game-search").is(":hover"))) {
        $("#result-games").show();
    }
});

$('.nav-tabs').on('shown.bs.tab', 'a', function (e) {
    if (e.target) {
        var elems = document.querySelectorAll(".nav-item .active");
        [].forEach.call(elems, function (el) {
            if (e.target !== el) {
                $(el).removeClass("active");
            }
        });
        $(e.target).addClass('active');
    }
});

$('.faux-select').click(function () {
    $(this).toggleClass('open');
    $('.options', this).toggleClass('open');
});

$('.options li').click(function () {
    var selection = $(this).text();
    var dataValue = $(this).attr('data-value');
    $('.selected-option span').text(selection);
    $('.faux-select').attr('data-selected-value', dataValue);
    changeLang(dataValue);
});

$(function () {
    $('.nav-tabs').responsiveTabs();
});

function changePlayDemoSrc(event) {
    let url = event.getAttribute("data-video");
    document.getElementById('playDemoFrame').setAttribute('src', url);
    document.getElementById('demo-overlay').classList.remove('hide-me');
    document.getElementById('demo-overlay').classList.add('table-me');
}

function emptyPlayDemoSrc() {
    var noDemoLoc = "about:blank";
    document.getElementById('playDemoFrame').setAttribute('src', noDemoLoc);
    document.getElementById('demo-overlay').classList.remove('table-me');
    document.getElementById('demo-overlay').classList.add('hide-me');
}

function changeLang(dataValue) {
    let href = window.location.href;

    function clearPrevParam() {
        let startParams = href.indexOf("?");
        let langParam;
        let langParamIndex = href.indexOf("lang", startParams);
        if (startParams !== -1) {
            if (startParams + 1 === langParamIndex) {
                if (href.indexOf("&", startParams) !== -1) {
                    langParam = href.substr(startParams + 1, href.indexOf("&") - startParams);
                    href = href.replace(langParam, "");
                } else {
                    langParam = href.substr(startParams + 1, href.length - startParams);
                    href = href.replace("?" + langParam, "");
                }
            } else {
                if (langParamIndex !== -1) {
                    langParam = href.substr(langParamIndex - 1,
                        href.indexOf("&", href.indexOf("lang")) - langParamIndex + 1);
                    if (langParam === "") {
                        langParam = href.substr(langParamIndex - 1, href.length);
                    }
                    href = href.replace(langParam, "");
                }
            }
        }
    }

    function trimAnchor() {
        let index = href.indexOf("#");
        if (index !== -1) {
            href = href.slice(0, index);
        }
    }

    if (dataValue === "chinese") {
        if (href.includes("lang=zh-cn")) {
            return false;
        }
        trimAnchor();
        clearPrevParam();
        if (href.indexOf("?") !== -1) {
            href = href + "&lang=zh-cn";
        } else {
            href = href + "?lang=zh-cn";
        }

    } else {
        if (href.includes("lang=en") || !href.includes("lang")) {
            return false;
        }
        trimAnchor();
        clearPrevParam();
    }
    document.location.href = href;
}
