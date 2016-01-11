/* 
 * Main js file to create the interactions
 */
var gridSize = 10;
var tbl = $("#grid");

$('#grid').addClass('grid');

var i = 0;
for (var r = 0; r < gridSize; ++r) {
  var tr = $('<tr></tr>');
  tbl.append(tr);
  for (var c = 0; c < gridSize; ++c) {
    var cell = $('<td></td>', {
      "class": "grid",
      "rowNum": r,
      "ColNum": c,
      text: 0,
      click: function() {
        updateRowCol($(this).attr("rowNum"), $(this).attr("colNum"));
      }
    })
    tr.append(cell);
  }
}

function updateRowCol(row, col) {
  $("td").filter(function(index) {
    return $(this).attr("rowNum") === row || $(this).attr("colNum") === col;
  }).animate({
    backgroundColor: "#FFFF00"
  }, 500, function() {
    $(this).animate({
      backgroundColor: '#D0D0D0'
    }, 500)
  });

  console.log("Click detected on (" + row + ", " + col + ")")
}

      
