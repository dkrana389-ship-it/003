# Call History App - GitHub se APK banane ke steps

1. github.com par naya repository banao (naam: call-history-app).
2. Is zip ko unzip karo. Andar ke SAARE files/folders (app_src, .github, README.md) repository me upload karo.
   - ".github" folder hidden hota hai. Agar upload na ho to GitHub par
     "Add file > Create new file" me naam likho:  .github/workflows/build.yml
     aur build.yml ka content paste karo.
3. Repository me "Actions" tab kholo. "Build APK" workflow chalega (5-10 min).
   Na chale to "Build APK" > "Run workflow" dabao.
4. Green tick aane par us run ko kholo. Neeche "Artifacts" me call-history-apk download karo.
5. Zip se app-release.apk nikalo, phone me install karo.
6. App kholo, 3 permissions do, phir kisi ko call karke test karo.
