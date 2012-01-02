-module(day3).
-export([translate/1]).
-export([doctor_loop/0]).
-export([nurse_loop/0]).

%Doc = spawn(fun day3:doctor_loop/0).
%Doc ! start.
%day3:translate("casa").
%translator ! {self(), "die"}.
%doctor ! die.
%nurse ! die.


translate_loop() ->
    receive
        {From, "casa"} ->
            From ! "house",
            translate_loop();

        {From, "blanca"} ->
            From ! "white",
            translate_loop();

        {_, "die"} ->
            exit(foobar);

        {From, _} ->
            From ! "I don't understand.",
            translate_loop()
    end.

translate(Word) ->
    translator ! {self(), Word},
    receive
        Translation -> Translation
    end.

doctor_loop() ->
    process_flag(trap_exit, true),
    receive
        start ->
            self() ! new_nurse,
            self() ! new_translator,
            doctor_loop();

        die ->
            exit(die);

        new_translator ->
            case lists:member(translator, registered()) of
                false ->
                    io:format("Creating a new translator.~n"),
                    register(translator, spawn_link(fun translate_loop/0));
                true ->
                    io:format("Translater already alive.~n")
            end,
            doctor_loop();

        new_nurse ->
            case lists:member(nurse, registered()) of
                false ->
                    io:format("Creating a new nurse.~n"),
                    register(nurse, spawn_link(fun nurse_loop/0));
                true ->
                    io:format("Nurse already alive.~n")
            end,
            doctor_loop();

        {'EXIT', What, Reason} ->
            io:format("Something ~p died with reason ~p.~n", [What, Reason]),
            self() ! start,
            doctor_loop()
    end.

nurse_loop() ->
    process_flag(trap_exit, true),
    receive
        die ->
            exit(die);

        new_doctor ->
            case lists:member(doctor, registered()) of
                false ->
                    io:format("Creating a new doctor.~n"),
                    register(doctor, spawn_link(fun doctor_loop/0)),
                    doctor ! start;
                true ->
                    io:format("Doctor already alive.~n")
            end,
            nurse_loop();

        {'EXIT', What, Reason} ->
            io:format("Something ~p died with reason ~p.~n", [What, Reason]),
            self() ! new_doctor,
            nurse_loop()
    end.

