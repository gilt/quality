$(function(){
  $.ajax({
            type: 'GET',
            url: '/gilt/teams/architecture/member_summary',
            dataType: 'json',
            success: function(data) {
                $('#team_architecture_number_members').html(data.number_members);
            }
          });
});
