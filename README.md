# Printing Zebra

Zebra (ZPL) label printer implementation for the CommerceLink platform.

Implements the `PrintProviderDescriptor` SPI from [printing-api](https://github.com/commerce-link/printing-api), rendering domain label models into ZPL for Zebra desktop printers (e.g. ZD220 / ZD230). The current scope is warehouse labels printed from goods-in (PZ) documents.

The provider produces raw ZPL only — it does not talk to the printer. Delivery to the device is handled client-side via [Zebra Browser Print](https://www.zebra.com/gb/en/support-downloads/software/printer-software/browser-print.html), which must be installed on the operator's workstation to send the ZPL to a locally connected (USB) Zebra printer.

## Workstation setup (USB printing)

Printing is client-side: the app generates ZPL and the operator's browser sends it to the locally connected USB Zebra printer. Each printing workstation needs **two Zebra tools** installed (both free, from [zebra.com](https://www.zebra.com/) → Support & Downloads):

1. **Zebra Setup Utilities** — installs the Windows printer driver and lets you connect the USB printer, set the label/media size and calibrate it. Use it once to get the printer recognized and configured for your label stock.

2. **Zebra Browser Print** — a small background agent that exposes a local HTTP service (default `http://localhost:9100`) which the web page calls to push ZPL to the USB printer. Without it, the in-app "Print labels" action fails with a network error (`Failed to fetch`). After installing: run it, confirm the USB printer shows up as a device, and add the app's domain to the allowed origins (CORS) in its settings.

The **Device ID** entered per printer profile in the app (warehouse settings) is the `uid` reported by Browser Print at `http://localhost:9100/available`.

## Provider Discovery

This library is discovered at runtime via `ServiceLoader`. See the [provider-api README](https://github.com/commerce-link/provider-api) for registration details.
