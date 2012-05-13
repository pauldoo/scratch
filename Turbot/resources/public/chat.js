function tick() {
    $.getJSON("chat.json", function(d) {
        var channel = "#" + $.query.get('channel');
        var data = d[channel];

        $('title').text(channel);

        $('#channelname').empty();
        $('<h1/>').text(channel).appendTo($('#channelname'));
        $('#channeltopic').empty();
        $('<p/>').text(data['topic']).appendTo($('#channeltopic'));

        $('#userlist ul').remove();
        var userlistul = $('<ul/>');
        $.each(data.users, function(i, user) {
            $('<li/>').text(user).appendTo(userlistul);
        });
        userlistul.appendTo($('#userlist'));

        $('#chatlog').empty();
        $.each(data.recentlines, function(i, line) {
            var p = $("<p/>");
            if (line.doing == 'PRIVMSG') {
                $('<span class="first nick"/>').text(line.nick).appendTo(p);
                $('<span>').text(line.message).appendTo(p);
                if (line.isaction) {
                    p.addClass("action");
                    p.find(".nick").append("&nbsp;");
                } else {
                    p.find(".nick").append(":&nbsp;");
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
            } else if (line.doing == 'TOPIC') {
                p.addClass("action");
                $('<span class="first nick"/>').text(line.nick).appendTo(p);
                $('<span/>').text(" has changed the topic to: ").appendTo(p);
                $('<span/>').text(line.topic).appendTo(p);
            }

            if (p.children().length > 0) {
                var timestamp = new Date(line.timestamp);
                $('<span class="timestamp"/>').text(timestamp.toLocaleTimeString()).appendTo(p);
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

