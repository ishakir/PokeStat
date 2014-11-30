var legendTemplate = '<div class="btn-group-vertical" role="group"> \
                        <% for (var i=0; i<datasets.length; i++){%> \
                          <button type="button" id="<%=datasets[i].label%>" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false" onclick="removePokemon(this.id)" style="color: <%=datasets[i].strokeColor%>;"> \
                          <%if(datasets[i].label){%> \
                            <%=datasets[i].label%> \
                          <%}%> \
                          <span class="glyphicon glyphicon-remove" onclick="hideErrorMessage()"></button> \
                        <%}%> \
                      </div>';

var options = { 
  pointDot: false, 
  datasetFill: false,
  showTooltips: false,
  legendTemplate: legendTemplate
};
var fillColor = "rgba(0,0,0,0)";

var displayTier = "";
var tier = "";

var all_pokemon;

var pokemon = {};

var months = [
  "january",
  "february",
  "march",
  "april",
  "may",
  "june",
  "july",
  "august",
  "september",
  "october"
];

var free_colors = [
  "rgb(255,   0,   0)",
  "rgb(  0, 255,   0)",
  "rgb(  0,   0, 255)",
  "rgb(  0,   0,   0)",
  "rgb(  0, 255, 255)",
  "rgb(255,   0, 255)"
];

$(document).ready( function () {

  $.ajaxSetup({
    async: false
  });

  changeTier("OU");

  redrawChart();
  
});

var changeTier = function(newTier) {
  
  displayTier = newTier;
  tier = newTier.toLowerCase();

  // Move all colours back into the color map, make pokemon empty
  $.each(Object.keys(pokemon), function(index, name) {
    free_colors.unshift(pokemon[name]);
    delete pokemon[name];
  });

  // Replace the pokemon data
  $.getJSON("/api/pokemon/" + tier, function(data) {
    all_pokemon = data;
  });

  $('#pickerColumn').empty();
  $('#pickerColumn').html(
    '<div id="pokemonPicker" style="text-align: center;"> \
      <input id="pickerInput" class="typeahead" type="text" placeholder="Add a pokemon..."> \
    </div>'
  )
  $('#pokemonPicker .typeahead').typeahead({
      hint: false,
      highlight: true,
      minLength: 3
    },
    {
      name: 'pokemon',
      displayKey: 'value',
      source: substringMatcher(all_pokemon)
  }).bind('typeahead:selected', function($e, pokemon){
    $('#pickerInput').val('');
    addPokemon(pokemon.value)
  });

  hideChart();
  displayUsageInfo();

  $('#tierSmall').html("2014 statistics for the "+displayTier+" tier");
  $('#tierSelected').html("You currently have the "+displayTier+" tier selected, to change tier, click on the dropdown on the right.")
  $('#tierDropDown').html(displayTier+'\n<span class="caret"></span>');

}

var redrawChart = function() {
  pokemon_to_plot = Object.keys(pokemon);
  if(pokemon_to_plot.length >= 6) {
    $('#pokemonPicker').hide();
  } else if(pokemon_to_plot.length > 0) {
    $('#pickerInput').val('');
    $('#pokemonPicker').show();
  } else {
    displayUsageInfo();
    hideChart();
    return;
  }

  hideUsageInfo();
  showChart();

  var pokemon_data = $.map(pokemon_to_plot, function(name, index) {
    var json_data;
    $.getJSON("/api/pokemon/"+name+"/"+tier+"/usage", function(data) {
      json_data = data;
    });
    var months_data = $.map(months, function(month, index) {
      if(json_data[month]) {
        return json_data[month];
      } else {
        return (json_data[months[index - 1]] + json_data[months[index + 1]]) / 2;
      }
    });
    return {
      label: name,
      strokeColor: pokemon[name],
      data: months_data
    }
  });


  var data = {
    labels: months,
    datasets: pokemon_data
  };

  var usageChart = new Chart($('#usageChart').get(0).getContext("2d")).Line(data, options);

  $('#usageLegend').html(usageChart.generateLegend())
}

var addPokemon = function(name) {
  if(pokemon[name]) {
    showErrorMessage(name + " is already on the chart!");
    return;
  }
  var newColor = free_colors.shift();
  pokemon[name] = newColor;
  redrawChart();
}

var removePokemon = function(name) {
    var color = pokemon[name];
    delete pokemon[name];
    free_colors.unshift(color);
    redrawChart();
}

var displayUsageInfo = function() {
  $('#usageRow').show();
}

var hideUsageInfo = function() {
  $('#usageRow').hide();
}

var showChart = function() {
  $('#chartRow').show();
}

var hideChart = function() {
  $('#chartRow').hide();
}

var showErrorMessage = function(message) {
  $('#errorMessage').html(message);
  $('#errorMessageRow').show();
}

var hideErrorMessage = function() {
  $('#errorMessageRow').hide();
}

var substringMatcher = function(strs) {
  return function findMatches(q, cb) {
    var matches, substrRegex;
 
    // an array that will be populated with substring matches
    matches = [];
 
    // regex used to determine if a string contains the substring `q`
    substrRegex = new RegExp(q, 'i');
 
    // iterate through the pool of strings and for any string that
    // contains the substring `q`, add it to the `matches` array
    $.each(strs, function(i, str) {
      if (substrRegex.test(str)) {
        // the typeahead jQuery plugin expects suggestions to a
        // JavaScript object, refer to typeahead docs for more info
        matches.push({ value: str });
      }
    });
 
    cb(matches);
  };
};