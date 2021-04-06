#pragma version(1)
#pragma rs java_package_name(me.minetsh.imaging.rs)

void init() {
}

void root(const uchar4 *in, uchar4 *out, uint32_t x, uint32_t y) {
    // a 是透明度，这里不修改透明度。
    out->a = in->a;

    // 快，但并不是真正意义的去色
    out->r = out->g = out->b = (in->r + in->g + in->b) / 3;

    // 慢，但是是真正的去色
    // out->r = out->g = out->b = (in->r * 299 + in->g * 587 + in->b * 114 + 500) / 1000;
}

/*
 * 黑金色转换
 */
uchar4 __attribute__((kernel)) blackGold(uchar4 in, uint32_t x, uint32_t y) {
    uchar4 out = in;

    if ((in.r < in.b) && (in.g < in.b)) {
        out.r = out.g = out.b = (in.r*299 + in.g*587 + in.b*114 + 500) / 1000;
    }

    return out;
}

uchar4 __attribute__((kernel)) invert(uchar4 in) {
    uchar4 out = in;
    out.r = 255 - in.r;
    out.g = 255 - in.g;
    out.b = 255 - in.b;
    return out;
}