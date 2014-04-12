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
#define NEXTWAVE_FONT FONT_KEY_ROBOTO_BOLD_SUBSET_49
#define FONT_HEIGHT   49
#define TOPBAR_HEIGHT 20
#define TIME_FORMAT "%M:%S"

/*
 * STATIC GLOBALS
 */
/* Every pebble app has a window and at least one text layer */
static Window *window;
static TextLayer *text_layer;
/* Frame to create text layer size */
static GRect timeFrame = {
    .origin = {
        .x = 0,
        .y = ((168 + TOPBAR_HEIGHT)/2) - (FONT_HEIGHT)
    },
    .size = {
        .w = 144,
        .h = 168
    }
};
/* Stored Time */
static char time_text[] = "00:00";
static uint32_t seconds_left = 10;

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
 * TIME RENDERING
 */
static void
render_time (uint32_t s)
{
    struct tm t;
    t.tm_min = s / 60;
    t.tm_sec = s % 60;

    strftime(time_text, sizeof(time_text), TIME_FORMAT, &t);
}

/*
 * TIME HANDLING
 */
static void
handle_second_tick (struct tm* tick_time, TimeUnits units_changed)
{
    if (seconds_left != 0)
    {
        seconds_left--;
    }
    if (seconds_left == 0)
    {
        vibes_long_pulse();
    }

    render_time(seconds_left);
    text_layer_set_text(text_layer, time_text);
}


static void
window_load (Window *window)
{
    Layer *window_layer = window_get_root_layer(window);
    GRect bounds = layer_get_bounds(window_layer);

    text_layer = text_layer_create(timeFrame);
    text_layer_set_font(text_layer, fonts_get_system_font(NEXTWAVE_FONT));
    text_layer_set_text(text_layer, "00:00");
    text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
    layer_add_child(window_layer, text_layer_get_layer(text_layer));

    // handle_second_tick(current_time, SECOND_UNIT);
    tick_timer_service_subscribe(SECOND_UNIT, &handle_second_tick);
}

static void
window_unload (Window *window)
{
    text_layer_destroy(text_layer);
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
