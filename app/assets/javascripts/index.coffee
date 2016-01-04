$ ->
  $.get "/exams", (exams) ->
    $.each exams, (index, exam) ->
      name = $("<div>").addClass("name").text exam.name
      level = $("<div>").addClass("age").text exam.level
      date = $("<div>").addClass("age").text exam.date
      $("#persons").append $("<li>").append(name).append(level).append(date)