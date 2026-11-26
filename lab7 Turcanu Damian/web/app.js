const statusNode = document.querySelector("#service-state");

async function inspectService(path, expectedHeader) {
  const response = await fetch(path, { method: "HEAD", cache: "no-store" });
  const origin = response.headers.get("X-Asset-Source");
  return response.ok && origin === expectedHeader;
}

async function showServiceStatus() {
  if (!statusNode) return;
  try {
    const results = await Promise.all([
      fetch("/api/health", { cache: "no-store" }),
      inspectService("/assets/images/avatar.svg", "images-container"),
      inspectService("/assets/styles/style.css", "styles-container")
    ]);
    const web = await results[0].json();
    const ready = web.status === "ok" && results[1] && results[2];
    statusNode.innerHTML = '<span class="status-pulse ' + (ready ? "" : "status-error") + '"></span>' +
      (ready ? "3 containere web răspund" : "Un serviciu nu răspunde");
    statusNode.setAttribute("aria-label", ready
      ? "Toate cele trei containere web sunt active"
      : "Verificare incompletă a serviciilor");
  } catch {
    statusNode.innerHTML = '<span class="status-pulse status-error"></span> Serviciile nu sunt disponibile';
  }
}

showServiceStatus();
