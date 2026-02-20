function Sprite() {
    let that = this;

    this.x = 0;
    this.y = 0;

    this.size_x = 0;
    this.size_y = 0;

    this.engine = null;
    this.texture = null;

    this.frame_x = 0;
    this.frame_y = 0;
    this.frame_width = 0;
    this.frame_height = 0;

    this.frame_index = 0;
    this.frames_count = 1;
    this.frames_in_line_x = 1;
    this.frames_in_line_y = 1;

    this.canvas = null;
    this.context = null;

    this.state = 0;

    // For sprite drawing as reels
    this.reels_count = 3;
    this.icons_in_reel = 3;
    this.offset_x = 0;
    this.offset_y = 0;
    this.spacing_x = 0;
    this.spacing_y = 0;
    this.internal_offsets_y = [0, 0, 0, 0, 0];
    this.reels_heights = [3, 3, 3, 3, 3];

    this.callback_event_click = null;
    this.callback_event_enter = null;
    this.callback_event_move = null;
    this.callback_event_leave = null;

    this.setClickEvent = function (callback) {
        that.callback_event_click = callback;
    };
    this.addMouseEnterEvent = function (callback) {
        that.callback_event_enter = callback;
    };
    this.addMouseMoveEvent = function (callback) {
        that.callback_event_move = callback;
    };
    this.addMouseLeaveEvent = function (callback) {
        that.callback_event_leave = callback;
    };

    this.onClick = function () {
        if (that.callback_event_click != null) that.callback_event_click();
    };


    this.onMove = function (x, y) {
        if (that.callback_event_move != null) that.callback_event_move(that, x, y, x - (that.x + that.offset_x), y - (that.y + that.offset_y));
    };

    this.onEnter = function (x, y) {
        that.state = 1;
        if (that.callback_event_enter != null) that.callback_event_enter(that, x, y, x - (that.x + that.offset_x), y - (that.y + that.offset_y));
    };

    this.onLeave = function (x, y) {
        that.state = 0;
        if (that.callback_event_leave != null) that.callback_event_leave(that, x, y, x - (that.x + that.offset_x), y - (that.y + that.offset_y));
    };

    this.isMouseInside = function () {
        return (that.state == 1);
    };

    this.isContains = function (x, y) {
        if ((x > that.x && y > that.y) && (x <= that.x + that.size_x && y <= that.y + that.size_y)) {
            return true;
        }

        return false;
    };

    this.setSize = function (size_x, size_y) {
        that.size_x = size_x;
        that.size_y = size_y;
    };

    this.setFrameSize = function (size_x, size_y) {
        that.frame_width = size_x;
        that.frame_height = size_y;
    };

    this.setFrameIndex = function (index) {
        that.frame_index = index;
        if (that.frame_index < 0) that.frame_index += that.frames_count;
        if (that.frame_index >= that.frames_count) that.frame_index -= that.frames_count;
    };

    this.prevFrame = function () {
        that.setFrameIndex(that.frame_index - 1);
    };
    this.nextFrame = function () {
        that.setFrameIndex(that.frame_index + 1);
    };

    this.setTexture = function (texture) {
        that.texture = texture;
    };

    this.setPosition = function (x, y) {
        that.x = Math.round(x);
        that.y = Math.round(y);
    };

    this.setAsReels = function (reels_count, icons_in_reel, offset_x, offset_y, spacing_x, spacing_y) {
        that.reels_count = reels_count;
        that.icons_in_reel = icons_in_reel;
        that.offset_x = offset_x;
        that.offset_y = offset_y;
        that.spacing_x = spacing_x;
        that.spacing_y = spacing_y;
    };

    this.setInternalOffsetsY = function (internal_offsets_y) {
        that.internal_offsets_y = internal_offsets_y;
    };

    this.setReelsHeights = function (reels_heights) {
        that.reels_heights = reels_heights;
    };

    this.init = function (engine, canvas, context) {
        that.canvas = canvas;
        that.context = context;
        that.engine = engine;

        that.splitFrames(1, 1, 1);
    };

    this.splitFrames = function (frames_count, frames_in_line_x, frames_in_line_y) {
        that.frames_count = frames_count;
        that.frames_in_line_x = frames_in_line_x;
        that.frames_in_line_y = frames_in_line_y;

        that.frame_width = that.texture.getWidth() / frames_in_line_x;
        that.frame_height = that.texture.getHeight() / frames_in_line_y;

        that.size_x = that.frame_width;
        that.size_y = that.frame_height;
    };

    this.draw = function (x, y, rotation_angle, rotation_point_x, rotation_point_y) {
        that.engine.addSprite(that);

        let draw_x = (x != undefined) ? x : that.x;
        let draw_y = (y != undefined) ? y : that.y;

        let draw_rotation_angle = (rotation_angle != undefined) ? rotation_angle : 0;
        let draw_rotation_point_x = (rotation_point_x != undefined) ? rotation_point_x : draw_x + that.frame_width / 2;
        let draw_rotation_point_y = (rotation_point_y != undefined) ? rotation_point_y : draw_y + that.frame_height / 2;

        that.setPosition(draw_x, draw_y);
        that.drawFrame(that.frame_index, draw_x, draw_y, draw_rotation_angle, draw_rotation_point_x, draw_rotation_point_y);
    };

    this.drawFrame = function (frame_index, x, y, rotation_angle, rotation_point_x, rotation_point_y) {
        let draw_x = (x != undefined) ? x : that.x;
        let draw_y = (y != undefined) ? y : that.y;

        let draw_rotation_angle = (rotation_angle != undefined) ? parseFloat(rotation_angle) : 0;
        let draw_rotation_point_x = (rotation_point_x != undefined) ? rotation_point_x : draw_x + that.frame_width / 2;
        let draw_rotation_point_y = (rotation_point_y != undefined) ? rotation_point_y : draw_y + that.frame_height / 2;

        that.setPosition(draw_x, draw_y);

        if (that.texture != null) {
            that.frame_x = that.frame_width * (frame_index % that.frames_in_line_x);
            that.frame_y = that.frame_height * Math.floor(frame_index / that.frames_in_line_x);

            if (draw_rotation_angle === 0) {
                that.context.drawImage(that.texture.getImage(), that.frame_x, that.frame_y, that.frame_width, that.frame_height,  // Texture region
                    that.x, that.y, that.size_x, that.size_y);       // Sprite parameters
            }
            else {
                that.context.save();
                that.context.translate(draw_rotation_point_x, draw_rotation_point_y);
                that.context.rotate(draw_rotation_angle * Math.PI / 180);
                that.context.drawImage(that.texture.getImage(), that.frame_x, that.frame_y, that.frame_width, that.frame_height,  // Texture region
                    -that.frame_width / 2, -that.frame_height / 2, that.size_x, that.size_y);       // Sprite parameters
                that.context.restore();
            }
        }
    };

    this.drawArray = function (stop_reel_array, isLeftToRight, isInternalOffsetsY) {
        if (stop_reel_array == null) return;
        let isOffsetsY = (isInternalOffsetsY != undefined) ? isInternalOffsetsY : false;

        for (let i = 0; i < that.reels_count; i++) {
            for (let j = 0; j < that.icons_in_reel; j++) {
                let index = 0;

                if (isLeftToRight === undefined || isLeftToRight === false) {
                    index = i * that.icons_in_reel + j;
                } else {
                    index = j * that.reels_count + i;
                }

                that.x = that.offset_x + i * (that.size_x + that.spacing_x);
                that.y = that.offset_y + j * (that.size_y + that.spacing_y)
                    + (isOffsetsY ? that.internal_offsets_y[i] : 0);
                that.drawFrame(stop_reel_array[index]);
            }
        }
    };

    this.drawArrayDiamond = function (stop_reel_array, isInternalOffsetsY, reels_heights) {
        if (stop_reel_array == null) return;
        let isOffsetsY = (isInternalOffsetsY != undefined) ? isInternalOffsetsY : false;
        let reelsHeights = (reels_heights != undefined) ? reels_heights : that.reels_heights;

        let index = 0;
        for (let i = 0; i < that.reels_count; i++) {
            for (let j = 0; j < reelsHeights[i]; j++) {
                that.x = that.offset_x + i * (that.size_x + that.spacing_x);
                that.y = that.offset_y + j * (that.size_y + that.spacing_y)
                    + (isOffsetsY ? that.internal_offsets_y[i] : 0);
                that.drawFrame(stop_reel_array[index + j]);
            }
            index += reelsHeights[i];
        }
    };

    this.drawLines = function (array_payout, WIN_LINES, COLORS, line_size, offset_y, isWrap, isInternalOffsetsY) {
        let lines_offset_y = (offset_y != undefined) ? offset_y : 0;
        let lines_isWrap = (isWrap != undefined) ? isWrap : false;
        let lines_array_payout = (array_payout != undefined) ? array_payout : that.engine.array_payout;
        let lines_WIN_LINES = (WIN_LINES != undefined) ? WIN_LINES : that.engine.WIN_LINES;
        let lines_COLORS = (COLORS != undefined) ? COLORS : that.engine.COLORS;
        let lines_line_size = (line_size != undefined) ? line_size : that.engine.line_thickness;
        let isOffsetsY = (isInternalOffsetsY != undefined) ? isInternalOffsetsY : false;

        let wrap = 1;
        if (lines_isWrap) wrap = -1;

        let win_count = 0;
        let iLinesPayout = lines_array_payout.map(v=>parseInt(v));
        for (let z = 0; z < iLinesPayout.length; z++) {
            if ((iLinesPayout[z] > 0) || (that.engine.getSpecialInfoValue(iLinesPayout[z]) != null)) {
                win_count += 1;
            }
        }

        if (win_count > 0) {
            let line_index = 0;
            for (let i = 0; i < iLinesPayout.length; i++) {
                if (iLinesPayout[i] !== 0) {
                    let icon_indexes = lines_WIN_LINES[i % lines_WIN_LINES.length];

                    let position_array_x = [];
                    let position_array_y = [];

                    let line_offset = 0;
                    let line_step = 0;

                    line_offset = -(win_count - 1);
                    if (win_count <= 10) line_offset += line_index * 2;
                    else line_offset = Math.round(line_offset / 2) + line_index;

                    for (let j = 0; j < icon_indexes.length; j++) {
                        if (Math.abs(icon_indexes[j]) >= 100) continue;
                        let x = that.offset_x + that.size_x / 2 + j * (that.size_x + that.spacing_x);
                        let y = that.offset_y + that.size_y / 2 + (lines_offset_y + icon_indexes[j] * wrap)
                            * (that.size_y + that.spacing_y) - line_offset + (isOffsetsY ? that.internal_offsets_y[j] : 0);

                        position_array_x.push(Math.round(x));
                        position_array_y.push(Math.round(y));
                    }

                    let color;
                    if (lines_COLORS === COLOR_RANDOM) color = setColor(getRandom(0, 255), getRandom(0, 255), getRandom(0, 255), 1.0);
                    else color = lines_COLORS[i % lines_COLORS.length];

                    that.engine.drawPolygon(position_array_x, position_array_y, COLOR_NULL, lines_line_size, color);

                    position_array_x.length = 0;
                    position_array_y.length = 0;

                    line_index += lines_line_size / 2;
                }
            }
        }
    };

    this.getFramePosition = function (index, isLeftToRight) {
        let position = {x: 0, y: 0};
        let i, j;

        if (isLeftToRight === undefined || isLeftToRight === false) {
            i = Math.floor(index / that.icons_in_reel);
            j = (index % that.icons_in_reel);
        }
        else {
            i = (index % that.reels_count);
            j = Math.floor(index / that.reels_count);
        }

        position.x = that.offset_x + i * (that.size_x + that.spacing_x);
        position.y = that.offset_y + j * (that.size_y + that.spacing_y);

        return position;
    }
}