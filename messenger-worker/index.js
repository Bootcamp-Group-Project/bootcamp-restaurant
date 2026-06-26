export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    // Meta webhook verification
    if (request.method === "GET") {
      const mode      = url.searchParams.get("hub.mode");
      const token     = url.searchParams.get("hub.verify_token");
      const challenge = url.searchParams.get("hub.challenge");
      if (mode === "subscribe" && token === env.VERIFY_TOKEN)
        return new Response(challenge, { status: 200 });
      return new Response("Forbidden", { status: 403 });
    }

    if (request.method === "POST") {
      const body = await request.json();
      if (body.object !== "page") return new Response("OK");

      const entry = body?.entry?.[0]?.messaging?.[0];
      if (!entry) return new Response("OK");

      const psid = entry.sender?.id;

      // ref comes from postback.referral (Get Started) or referral (returning user)
      const ref = entry.postback?.referral?.ref ?? entry.referral?.ref;

      if (!psid || !ref) return new Response("OK");

      // Query pickup status using the pickup code carried in ref
      const apiRes = await fetch(
        `${env.JAVA_API_BASE}/api/pickups/status?code=${encodeURIComponent(ref)}`
      );
      const data = apiRes.ok ? await apiRes.json() : null;

      let reply;
      if (!data || data.status === "NOT_FOUND") {
        reply = `找不到取餐碼 (${ref})，請確認後再試。`;
      } else if (data.status === "READY") {
        reply = `取餐碼 (${data.code}) 已完成，${data.expectedTime ?? ""} 可取餐。`;
      } else {
        reply = `取餐碼 (${data.code}) 這單號還在製作中，請稍候。`;
      }

      // Send Messenger reply
      await fetch("https://graph.facebook.com/v19.0/me/messages", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${env.PAGE_ACCESS_TOKEN}`,
        },
        body: JSON.stringify({
          recipient: { id: psid },
          message: { text: reply },
        }),
      });

      return new Response("OK");
    }

    return new Response("Method Not Allowed", { status: 405 });
  },
};
