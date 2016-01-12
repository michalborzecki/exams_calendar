$ ->
  $("#date_field").datepicker({dateFormat: "yy-mm-dd"})

$ ->
  if !$("#level_field").val()
    $("#level_field").val(1)

$ ->
  $( "#level_slider" ).slider({
    value: $("#level_field").val(),
    min: 1,
    max: 10,
    step: 1,
    slide: ( event, ui ) ->
      $( "#level_field" ).val( ui.value )
  })