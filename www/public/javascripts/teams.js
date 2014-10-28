$(function(){
  $('.team_number_members').each(function(el) {
    var teamKey = this.getAttribute("teamKey");
    $.ajax({
      type: 'GET',
      url: '/gilt/teams/' + teamKey + '/member_summary',
      dataType: 'json',
      success: function(data) {
        $('#team_' + teamKey + '_number_members').html(data.number_members);
      }
    });
  });
});
