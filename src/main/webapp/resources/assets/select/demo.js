/* ===========================================================
 *
 *  Name:          global.js
 *  Updated:       2014-04-15
 *  What?:         Random crap for the Select or Die demo
 *
 *  Oddny | Cogs 'n Kegs
 * =========================================================== */


function random_word() {
    var $words      = ['Frightened', 'By', 'Your', 'Own', 'Smell', 'Bitterness', 'Will', 'Run', 'You', 'Through']; // In FLames - Bullet Ride (http://www.youtube.com/watch?v=lDwqzGtjGMU)
    var $randomizer = $words[Math.floor(Math.random() * $words.length)];
    return $randomizer;
}

jQuery(document).ready(function ($) {
    $(".basic2").selectOrDie();

    

   

   

  

});
