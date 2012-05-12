function tick() {
    $.getJSON("chat.json", function(data) {
        $('#userlist').empty();
        $('#userlist').append('<ul/>');
        $.each(data.users, function(i, user) {
            var userList = $('#userlist ul');
            $(userlist).append('<li/>')
            $(userlist).find('li').last().text(user);
        });

        $('#chatlog').empty();
        $.each(data.recentlines, function(i, line) {
            var p = $("<p/>");
            if (line.doing == 'PRIVMSG') {
                $('<span class="first nick"/>').text(line.nick).appendTo(p);
                $('<span>').text(line.message).appendTo(p);
                if (line.isaction) {
                    p.addClass("action");
                } else {
                    p.find(".nick").append(":");
                }
            } else if (line.doing == 'NICK') {
                p.addClass("action");
                $('<span class="first nick"/>').text(line.nick).appendTo(p);
                $('<span/>').text(" is now known as ").appendTo(p);
                $('<span class="nick"/>').text(line.newnick).appendTo(p);
            } else if (line.doing == 'JOIN') {
                p.addClass("action");
                $('<span class="first nick"/>').text(line.nick).appendTo(p);
                $('<span/>').text(" has joined").appendTo(p);
            } else if (line.doing == 'PART') {
                p.addClass("action");
                $('<span class="first nick"/>').text(line.nick).appendTo(p);
                $('<span/>').text(" has left: ").appendTo(p);
                $('<span/>').text(line.reason).appendTo(p);
            } else if (line.doing == 'QUIT') {
                p.addClass("action");
                $('<span class="first nick"/>').text(line.nick).appendTo(p);
                $('<span/>').text(" has quit: ").appendTo(p);
                $('<span/>').text(line.reason).appendTo(p);
            } else if (line.doing == 'KICK') {
                p.addClass("action");
                $('<span class="first nick"/>').text(line.target).appendTo(p);
                $('<span/>').text(" was kicked by ").appendTo(p);
                $('<span class="nick"/>').text(line.nick).appendTo(p);
                $('<span/>').text(" ").appendTo(p);
                $('<span/>').text(line.message).appendTo(p);
            }

            if (p.children().length > 0) {
                p.appendTo($('#chatlog'));
            }
        });
        $('#chatlog').scrollTo( 'max' );
    });

    tock();
}
function tock() {
    // Poll every 3s
    setTimeout("tick()", 3000);
}

$(document).ready(function() {
    tick();
});

