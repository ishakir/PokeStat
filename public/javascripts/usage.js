/*
 *  Constants
 */
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
  legendTemplate: legendTemplate,
  scaleLabel: "<%=value%>%"
};

var months = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October"
];

var monthIndex = {
  january: 0,
  february: 1,
  march: 2,
  april: 3,
  may: 4,
  june: 5,
  july: 6,
  august: 7,
  september: 8,
  october: 9
}

/*
 * Variables that represent of the state of the page
 */
var displayTier = "";
var tier = "";

var allPokemon;

var pokemon = {};

var freeColours = [
  "rgb(255,   0,   0)",
  "rgb(  0, 255,   0)",
  "rgb(  0,   0, 255)",
  "rgb(  0,   0,   0)",
  "rgb(  0, 255, 255)",
  "rgb(255,   0, 255)"
];

/*
 * Code
 */
$(document).ready( function () {

  $.ajaxSetup({
    async: false
  });

  changeTier("OU");
  
});

var refreshPage = function() {
  var pokemonToPlot = Object.keys(pokemon);

  if(pokemonToPlot.length >= 6) {
    $('#pokemonPicker').hide();
  } else if(pokemonToPlot.length > 0) {
    $('#pickerInput').val('');
    $('#pokemonPicker').show();
  } else {
    displayUsageInfo();
    hideChart();
    return;
  }

  redrawChart();

};

var changeTier = function(newTier) {
  
  displayTier = newTier;
  tier = newTier.toLowerCase();

  // Move all colours back into the color map, make pokemon empty
  $.each(Object.keys(pokemon), function(index, name) {
    removePokemon(name);
  });

  // Replace the pokemon data
  $.getJSON("/api/pokemon/" + tier, function(data) {
    allPokemon = data;
  });

  $('#pickerColumn').empty();
  $('#pickerColumn').html(
    '<div id="pokemonPicker" style="text-align: center;"> \
      <input id="pickerInput" class="typeahead" type="text" placeholder="Add a pokemon..."> \
    </div>'
  );

  $('#pokemonPicker .typeahead').typeahead({
      hint: false,
      highlight: true,
      minLength: 3
    },
    {
      name: 'pokemon',
      displayKey: 'value',
      source: substringMatcher(allPokemon)
  }).bind('typeahead:selected', function($e, pokemon){
    $('#pickerInput').val('');
    addPokemon(pokemon.value)
  });

  refreshPage();

};

var redrawChart = function() {
  hideUsageInfo();
  showChart();

  var smallestMonthIndex = months.length;
  var largestMonthIndex = 0;

  Object.keys(pokemon).forEach(function(name) {
    Object.keys(pokemon[name].data).forEach(function(month) {
      var currentMonthIndex = monthIndex[month];
      if(currentMonthIndex > largestMonthIndex) {
        largestMonthIndex = currentMonthIndex;
      } else if(currentMonthIndex < smallestMonthIndex) {
        smallestMonthIndex = currentMonthIndex;
      }
    });
  });

  var monthsForAllPokemon = months.slice(smallestMonthIndex, largestMonthIndex + 1);

  var pokemonData = $.map(Object.keys(pokemon), function(name, index) {
    var pokemonUsageNumbers = pokemon[name].data;

    var monthsData = $.map(monthsForAllPokemon, function(month, index) {
      var currentMonth = month.toLowerCase();

      var lastMonth = months[monthIndex[currentMonth] - 1];
      var nextMonth = months[monthIndex[currentMonth] + 1];

      if(lastMonth) {
        lastMonth = lastMonth.toLowerCase();
      }
      if(nextMonth) {
        nextMonth = nextMonth.toLowerCase();
      }

      var currentMonthData = pokemonUsageNumbers[currentMonth];
      var lastMonthData = pokemonUsageNumbers[lastMonth];
      var nextMonthData = pokemonUsageNumbers[nextMonth];

      if(currentMonthData) {
        return currentMonthData;
      } else if(lastMonthData && nextMonthData) {
        return (lastMonthData + nextMonthData) / 2;
      } else {
        return 0;
      }
    });
    
    return {
      label: name,
      strokeColor: pokemon[name].color,
      data: monthsData
    }
  });

  var data = {
    labels: monthsForAllPokemon,
    datasets: pokemonData
  };

  var usageChart = new Chart($('#usageChart').get(0).getContext("2d")).Line(data, options);

  $('#usageLegend').html(usageChart.generateLegend())
};

var addPokemon = function(name) {
  if(pokemon[name]) {
    showErrorMessage(name + " is already on the chart!");
    return;
  }

  var newColor = freeColours.shift();

  pokemon[name] = {};
  pokemon[name].color = newColor;
  $.getJSON("/api/pokemon/"+name+"/"+tier+"/usage", function(data) {
    pokemon[name].data = data;
  });

  refreshPage();
};

var removePokemon = function(name) {
    freeColours.unshift(pokemon[name].color);
    delete pokemon[name];
    refreshPage();
};

var displayUsageInfo = function() {
  $('#tierSmall').html("2014 statistics for the "+displayTier+" tier");
  $('#tierSelected').html("You currently have the "+displayTier+" tier selected, to change tier, click on the dropdown on the right.")
  $('#tierDropDown').html(displayTier+'\n<span class="caret"></span>');
  $('#usageRow').show();
};

var hideUsageInfo = function() {
  $('#usageRow').hide();
};

var showChart = function() {
  $('#chartRow').show();
};

var hideChart = function() {
  $('#chartRow').hide();
};

var showErrorMessage = function(message) {
  $('#errorMessage').html(message);
  $('#errorMessageRow').show();
};

var hideErrorMessage = function() {
  $('#errorMessageRow').hide();
};

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