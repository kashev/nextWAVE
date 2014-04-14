/*
 * nextWAVE
 * A project for HackIllinois 2014
 *
 * Dario Aranguiz :: aranguizdario@gmail.com
 * Kashev Dalmia  :: kashev.dalmia@gmail.com
 * Brady Salz     :: brady.salz@gmail.com
 * Ahmed Suhyl    :: sulaimn2@illinois.edu
 * 
 * nextWAVE.c
 */


#include <pebble.h>

/*
 * CONSTANTS
 */
#define TIME_FONT     FONT_KEY_ROBOTO_BOLD_SUBSET_49
#define TIME_HEIGHT   49
#define LOGO_FONT     FONT_KEY_GOTHIC_24_BOLD
#define STATUS_FONT   FONT_KEY_GOTHIC_18_BOLD
#define LOGO_HEIGHT   24
#define LOGO_TEXT     "nextWAVE"
#define TOPBAR_HEIGHT 20
#define TIME_FORMAT   "%M:%S"

#define INBOUND_SIZE  64
#define OUTBOUND_SIZE 64

#define STATUS_READY   "Scan your food!"
#define STATUS_COOKING "Food is cooking..."
#define STATUS_DONE    "Your food is done!"

/*
 * STATIC GLOBALS
 */
/* Every pebble app has a window and at least one text layer */
static Window *window;

/*
 * TEXT LAYERS
 *     LOGO
 *     TIME
 *     STATUS
 */
static TextLayer * logo_layer;
static TextLayer * time_layer;
static TextLayer * state_layer;

/* STATUS */
typedef enum {
    READY = 0x00,
    COOKING = 0x01,
    DONE = 0x02
} nextWaveState;
/* STORED STATE */
static nextWaveState current_state = READY;

/* Frame to create text layer size */
static GRect logoFrame = {
    .origin = {
        .x = 0,
        .y = 0
    },
    .size = {
        .w = 144,
        .h = LOGO_HEIGHT + 2
    }
};

static GRect timeFrame = {
    .origin = {
        .x = 0,
        .y = ((168 + TOPBAR_HEIGHT)/2) - (TIME_HEIGHT)
    },
    .size = {
        .w = 144,
        .h = 168
    }
};

static GRect statusFrame = {
    .origin = {
        .x = 0,
        .y = 100
    },
    .size = {
        .w = 144,
        .h = LOGO_HEIGHT + 2
    }
};

/*
 * TIME RENDERING
 */
/* Stored Time */
static char time_text[] = "00:00";
static uint32_t time_left = 0;
static void
render_time (uint32_t s)
{
    struct tm t;
    t.tm_min = s / 60;
    t.tm_sec = s % 60;

    strftime(time_text, sizeof(time_text), TIME_FORMAT, &t);
    text_layer_set_text(time_layer, time_text);
}

/*
 * STATE RENDERING
 */
static void
render_state (nextWaveState s)
{
    switch (s)
    {
        case READY:
            text_layer_set_text(state_layer, STATUS_READY);
            break;
        case COOKING:
            text_layer_set_text(state_layer, STATUS_COOKING);
            break;
        case DONE:
            text_layer_set_text(state_layer, STATUS_DONE);
            break;
        default:
            text_layer_set_text(state_layer, "ERROR");
            break;
    }
}


// /*
//  * CLICK HANDLING
//  */
// static void
// select_click_handler (ClickRecognizerRef recognizer, void *context)
// {
//     text_layer_set_text(text_layer, "BradyRocks");
// }

// static void
// up_click_handler (ClickRecognizerRef recognizer, void *context)
// {
//     text_layer_set_text(text_layer, "Up");
// }

// static void
// down_click_handler (ClickRecognizerRef recognizer, void *context)
// {
//     text_layer_set_text(text_layer, "Down");
// }

// static void
// click_config_provider (void *context)
// {
//     window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
//     window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
//     window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
// }


/*
 * TIME HANDLING
 */
/* SPECIAL VIBRATIONS */
// Vibe pattern: ON for 200ms, OFF for 100ms, ON for 400ms:
static const uint32_t const segments[] = { 500, 300, 500, 300, 500 };
static VibePattern pat = {
   .durations = segments,
   .num_segments = ARRAY_LENGTH(segments),
 };

static void
handle_second_tick (struct tm * tick_time, TimeUnits units_changed)
{
    if (current_state == COOKING)
    {
        if (time_left != 0)
        {
            time_left--;
        }
        if (time_left == 0)
        {
            vibes_enqueue_custom_pattern(pat);
            current_state = DONE;
        }

        render_state(current_state);
        render_time(time_left);
        text_layer_set_text(time_layer, time_text); 
    }
}


/*
 * APPSYNC
 */
static AppSync sync;
static uint8_t sync_buffer[32];

enum nextWaveAppSyncKeys
{
    NEXTWAVE_STATE_KEY = 0x00,
    NEXTWAVE_TIME_KEY  = 0x01
};

static void
sync_error_callback (DictionaryResult dict_error, AppMessageResult app_message_error, void *context)
{
    APP_LOG(APP_LOG_LEVEL_DEBUG, "App Message Sync Error: %d", app_message_error);
}

static void
sync_tuple_changed_callback (const uint32_t key,
                             const Tuple* new_tuple,
                             const Tuple* old_tuple,
                                   void * context)
{
    APP_LOG(APP_LOG_LEVEL_DEBUG, "RUNNING THE SYNC TUPLE CHANGED CALLBACK");
    switch (key)
    {
        case NEXTWAVE_STATE_KEY:
            render_state(new_tuple->value->uint8);
            current_state = new_tuple->value->uint8;
            APP_LOG(APP_LOG_LEVEL_DEBUG, "RENDERED THE STATE: %u", new_tuple->value->uint8);
            break;
        case NEXTWAVE_TIME_KEY:
            render_time(new_tuple->value->uint32);
            time_left = new_tuple->value->uint32;
            APP_LOG(APP_LOG_LEVEL_DEBUG, "RENDERED THE TIME: %u", (uint) new_tuple->value->uint32);
            break;
        default:
            APP_LOG(APP_LOG_LEVEL_DEBUG, "ERROR STATE");
            render_state(4);
    }
}


/*
 * UI SETUP
 */
static void
window_load (Window *window)
{
    Layer *window_layer = window_get_root_layer(window);

    /*
     * Logo
     * Time
     * Status
     */

    logo_layer  = text_layer_create(logoFrame);
    time_layer  = text_layer_create(timeFrame);
    state_layer = text_layer_create(statusFrame);

    text_layer_set_font(logo_layer,  fonts_get_system_font(LOGO_FONT));
    text_layer_set_font(time_layer,  fonts_get_system_font(TIME_FONT));
    text_layer_set_font(state_layer, fonts_get_system_font(STATUS_FONT));
    
    // text_layer_set_text(time_layer,  time_text);
    text_layer_set_text(logo_layer,  LOGO_TEXT);
    // text_layer_set_text(state_layer, STATUS_DONE);

    text_layer_set_text_alignment(time_layer,  GTextAlignmentCenter);
    text_layer_set_text_alignment(logo_layer,  GTextAlignmentCenter);
    text_layer_set_text_alignment(state_layer, GTextAlignmentCenter);

    layer_add_child(window_layer, text_layer_get_layer(time_layer));
    layer_add_child(window_layer, text_layer_get_layer(logo_layer));
    layer_add_child(window_layer, text_layer_get_layer(state_layer));

    /*
     * Set Second Tick Handler
     */
    tick_timer_service_subscribe(SECOND_UNIT, &handle_second_tick);

    /*
     * App Sync
     */
    Tuplet initial_values[] = {
        TupletInteger(NEXTWAVE_STATE_KEY, (uint8_t) READY),
        TupletInteger(NEXTWAVE_TIME_KEY, (uint8_t) 0),
    };

    app_sync_init(&sync,
                  sync_buffer,
                  sizeof(sync_buffer),
                  initial_values,
                  ARRAY_LENGTH(initial_values),
                  sync_tuple_changed_callback,
                  sync_error_callback,
                  NULL);
}

/*
 * UI TEARDOWN
 */
static void
window_unload (Window *window)
{
    app_sync_deinit(&sync);

    text_layer_destroy(logo_layer);
    text_layer_destroy(time_layer);
    text_layer_destroy(state_layer);
}

static void
init (void)
{
    window = window_create();
    
    // window_set_click_config_provider(window, click_config_provider);

    window_set_window_handlers(window, (WindowHandlers) {
        .load   = window_load,
        .unload = window_unload,
    });

    app_message_open(INBOUND_SIZE, OUTBOUND_SIZE);

    const bool animated = true;
    window_stack_push(window, animated);
}

static void
deinit (void)
{
    window_destroy(window);

}

int
main (void)
{
    init();

    APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);

    app_event_loop();
    deinit();
}
