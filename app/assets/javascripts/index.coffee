$ ->
  $.get "/exams", (exams) ->
    $.each exams, (index, exam) ->
      row = $("<tr>")
      name = $("<td>").addClass("name").text exam.name
      level = $("<td>").addClass("level").text exam.level
      date = $("<td>").addClass("date").text exam.date
      row.append(name).append(level).append(date)
      $("#exams").append(row)

$ ->
  $("#date").datepicker({dateFormat: "yy-mm-dd"})

$ ->
  $( "#level_slider" ).slider({
    value:1,
    min: 1,
    max: 10,
    step: 1,
    slide: ( event, ui ) ->
      $( "#level" ).val( ui.value )
  })

$ ->
  $("#level").val(1)