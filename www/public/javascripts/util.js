var deleteLink = document.querySelector('.delete');

deleteLink.addEventListener('click', function(event) {
    event.preventDefault();

    var text = this.getAttribute('data-confirm')
    var href = this.getAttribute('href');

    if (confirm(text)) {
        $('<form method="post" action="' + href + '"></form>').appendTo('body').submit();
    }
});
