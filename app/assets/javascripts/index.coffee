$ ->
  $.get "/exams", (exams) ->
    $.each exams, (index, exam) ->
      row = $("<tr>")
      name = $("<td>").addClass("name")
      name_a = $("<a>").attr("href", "/editexam/" + exam.id).text exam.name
      name.append(name_a)
      level = $("<td>").addClass("level").text exam.level
      date = $("<td>").addClass("date").text exam.date
      del = $("<td>").addClass("del")
      del_a = $("<a>").attr("href", "/deleteexam/" + exam.id).text "Delete"
      del.append(del_a)
      row.append(name).append(level).append(date).append(del)
      $("#exams").append(row)
