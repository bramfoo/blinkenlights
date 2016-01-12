/*
 * Main js file to create the interactions
 */
var gridSize = 50;
var tbl = $("#grid");

$('#grid').addClass('grid');

for (var r = 0; r < gridSize; r++) {
  var tr = $('<tr></tr>');
  tbl.append(tr);
  for (var c = 0; c < gridSize; c++) {
    var cell = $('<td></td>', {
      "class": "grid",
      "cellNum": r*gridSize + c,
      "rowNum": r,
      "ColNum": c,
      text: 0,
      click: function() {
        updateRowCol($(this).attr("rowNum"), $(this).attr("colNum"));
        reset42();
      }
    })
    tr.append(cell);
  }
}

/* This function selects all the cells that match the clicked cell's row or column,
 * and animates their background to yellow and back to the original grey;
 * it also adds one to the content of each of these cells.
 */
function updateRowCol(row, col) {
  $("td").filter(function(index) {
    return $(this).attr("rowNum") === row || $(this).attr("colNum") === col;
  }).animate({
    backgroundColor: "#FFFF00"
  }, 500, function() {
    $(this).animate({
      backgroundColor: '#D0D0D0'
    }, 500)
  }).map(function(i, el) {
    el.innerHTML = 1 + Number(el.innerHTML);
  });


  console.log("Click detected on (" + row + ", " + col + ")");
}


/* This function selects all cells that have value '4', and
 * checks their neighbours for value '2', if so resetting them
 */
function reset42() {
  var n = $("td").filter(function(index) {
    return $(this).html() === "4";}).length;

  // TODO:
  // * Check neighbours (those cells with cellNum +1, -1, +gridSize, -gridSize) for value 2
  //   * if so, animate and reset both

  console.log("There are " + n + " cells containing a 4");

}
