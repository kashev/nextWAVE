/*
 * nextWAVE
 * A project for HackIllinois 2014
 *
 * Dario Aranguiz :: aranguizdario@gmail.com
 * Kashev Dalmia  :: kashev.dalmia@gmail.com
 * Brady Salz     :: brady.salz@gmail.com
 * Ahmed Suhyl    :: sulaimn2@illinois.edu
 * 
 * Gruntfile.js
 */

/* GRUNT CONFIGURATION */
module.exports = function(grunt) {
  var html_files = {
    'dist/index.html' : 'src/index.html'
  };
  var css_files  = {
    'dist/css/main.css' : 'src/css/main.scss'
  };
  var js_files   = {
    'dist/js/main.js' : 'src/js/main.js',
  };
  
  var img_copy =  {
    flatten : true,
    expand  : true,
    cwd     : 'src/img',
    src     : ['**/*'],
    dest    : 'dist/img/'
  };

  /*
   * GRUNT INITIALIZATION CONFIGURATION
   */
  grunt.initConfig({
    
    /*
     * CONNECT
     *   live development server
     */
    connect: {
      server: {
        options: {
          base : 'dist',
          livereload : true,
          hostname: '0.0.0.0'   // uncomment for server available on LAN
          // hostname : localhost // uncomment for local only operation
        },
      }
    },

    /*
     * JSHINT
     *   making sure I don't write godawful JavaScript
     */
    jshint: {
      src: ['Gruntfile.js', 
            'src/js/main.js'],
      options: {
        curly : true,
        eqeqeq : true,
        immed : true,
        latedef : true,
        newcap : true,
        noarg : true,
        sub : true,
        undef : true,
        boss : true,
        eqnull : true,
        browser : true,
        globals : {
          $ : true,
          jQuery : true,
          require : true,
          define : true,
          requirejs : true,
          describe : true,
          expect : true,
          it : true,
          module  : true,
          console : true
        }
      }
    },

    /*
     * SASS
     *   the css preprocessor of choice
     */
    sass: {
      dev : {
        options : {
          style : 'expanded',
          trace : true,
          unixNewlines : true
        },
        files : css_files
      },
      dist : {
        options : {
          style : 'compressed',
          trace : true,
          unixNewlines : true
        },
        files : css_files
      }
    },

    /*
     * COPY
     *   for moving things around
     */
    copy : {
      dev : {
        files: [
          html_files,
          js_files,
          img_copy
        ]
      },
      dist : {
        files: [
          img_copy
        ]
      }
    },

    /*
     * HTMLMIN
     *   for production minifying of HTML
     */
    htmlmin : {
      dist : {
        options : {
          removeComments: true,
          collapseWhitespace: true
        },
        files : html_files
      }
    },

    /*
     * UGLIFY
     *   for production minifying of JavaScript
     */
    uglify : {
      dist : {
        files : js_files,
        options : {
          mangle : false,

        }
      }
    },

    /*
     * WATCH
     *   for live reload server
     */
    watch : {
      options : {
        livereload : true,
      },
      html : {
        files : ['src/*.html'],
        tasks : ['copy:dev'],
      },
      js : {
        files : ['Gruntfile.js', 'src/js/*.js'],
        tasks : ['jshint', 'copy:dev'],
      },
      css : {
        files : ['src/css/main.scss'],
        tasks : ['sass:dev'],
      },
      grunt : {
        files : ['Gruntfile.js'],
        tasks : ['jshint', 'copy:dev'],
      }
    }
  });

  /*
   * LOAD GRUNT TASKS
   */
  // Load JSHint Task
  grunt.loadNpmTasks('grunt-contrib-jshint');
  // Load Sass Compilation Task
  grunt.loadNpmTasks('grunt-contrib-sass');
  // Load Copy Tasks
  grunt.loadNpmTasks('grunt-contrib-copy');
  // Load Grunt Connect
  grunt.loadNpmTasks('grunt-contrib-connect');
  // Load Grunt Watch
  grunt.loadNpmTasks('grunt-contrib-watch');
  /* DIST ONLY TASKS */
  // Load HTML Minification Task
  grunt.loadNpmTasks('grunt-contrib-htmlmin');
  // Load JS Minification Task
  grunt.loadNpmTasks('grunt-contrib-uglify');


  /*
   * REGISTER GRUNT TASKS
   */
  // Default task is dev. dist builds deploy folder.
  grunt.registerTask('default', [
    'jshint',
    'sass:dev',
    'copy:dev'
  ]);
  
  grunt.registerTask('dist', [
    'jshint',
    'sass:dist',
    'htmlmin',
    'uglify',
    'copy:dist'
  ]);

  grunt.registerTask('server', [
    'jshint',
    'sass:dev',
    'copy:dev',
    'connect',
    'watch'
  ]);

};
