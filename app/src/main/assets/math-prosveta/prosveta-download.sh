# 1. go to https://bg.e-prosveta.bg/login and login
# 2. https://bg.e-prosveta.bg/resources/books choose a book
# 3. Click Увеличаване на фрагмент на страница аnd use it
# 4. Go to Dev Tools (F12) -> Sources, serch for web.e-prosveta.bg -> expand to bottom -> Copy link address
# 5. Paste over REPLACE_ME
# 6. Replace the page number in the url with '$i' e.g              ↓
# https://web.e-prosveta.bg/free-publication/161/large-page-image/$i?token=eyJh...
# 7. Check for the number of pages and replace REPLACE_ME_WITH_NUMBER_OF_PAGES with the number e.g {1..174} if the textbook has 174 pages

# 8. Run the script
# 8.1 Open terminal
# 8.2 navigate to the file location with command 'cd path_to_folder' e.g  
# 8.3 execute command 'bash ./prosveta-download.sh'

# 9. Check if the PDF is OK.
# 10. Upload the file in Google Drive
# 11. Rename it
# 12. Mark the textbook as converted in the Google Drive file with the list with all textbooks

for i in {1..114}; do
	wget --output-document=page$i.png https://web.e-prosveta.bg/free-publication/43/large-page-image/$i?token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXNzX2lkIjoxNjA3OTQzNywidXNlcl9zZXNzX2lkIjo2MDg2MTYsInN1YnNjcmlwdGlvbiI6ZmFsc2UsInN0YXRzX2lkIjo1MjU2NjY5MCwiZGV2aWNlX2lkIjozMDMxMSwiaWF0IjoxNzA3MjAzNjA1LCJleHAiOjE3MDcyMTQ0MDUsInN1YiI6IjYwODYxNiJ9.C8oVDb9wMSo_6B4gqzKp9kWsCG8F6g4KXhBetZ8zQ6owyOr_cG-0wmPw9BciDIBrPwlgHrb0NQL5qBYCHAwL-zCm0feHCmeZ-jDS1YR012ogwOy-4hMSreZityMw4n6e-RV16qPeF3czj7GhCKckutGFm-9fu6NHfcI2RrOJ528Jp3mSvJlcKCkNnE_qa3TMeTridDb7TVOwrzSXW0yywMHOJhKxi8b3CvsAx-CclOFGteJPZcdLZExdBV43aDPQif10edjUlCHddrg2GZOIrgx3cOUY264YOBueBDVc7xw0lVn8raXDx39KEKch2YedAWmfGonWuxkra4_Cgt0e3nCt3nEZPVUGNK13ya3Pi9thW0XGw_vEFOW3-ECntUVjwkuldg4IcM-bzmHlK_XPtEW3uUS2bFfeBzQTDC4fFPgNCZlSljTRCFUquNefT6pxyQjnT3dHsbiWkoK1TaED03-_vYrg55G8bd0K2WaRG7dUrzr3WRs7IJDMdq_2_P3ZWX5KQ4SP9FVaPQt90CJ2thdHym8FWFxiHvW0dAY3nDRENwKuQi_u63zlnanPUra2BVEPMOSJTgftzTjhcBDGcDzsmTkdb3qssDRBp9kc6r7bQirWyqIcy7-MsrxN8qUthM0xNXIApuo-DwtAvk0a-DwF1WPMsDtFwoBLxPrQC0Q
	sleep 5
done

rename -e 's/\d+/sprintf("%03d",$&)/e' -- *.png

converttopdf . textbook_name.pdf
