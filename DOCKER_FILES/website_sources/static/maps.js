function initMap() {
  // The location of Uluru
  const uluru = { lat: -25.344, lng: 131.036 };
  // The map, centered at Uluru
  const map = new google.maps.Map(document.getElementById("map"), {
    zoom: 4,
    center: uluru,
  });
  // The marker, positioned at Uluru
  ("{% for measurement in measurements -%}");
  var measurementPoints = {
    lat: parseFloat("{{measurement['gps_longitude']}}"),
    lng: parseFloat("{{measurement['gps_latitude']}}"),
  };
  new google.maps.Marker({
    position: measurementPoints,
    map: map,
  });
  ("{% endfor %}");
}
