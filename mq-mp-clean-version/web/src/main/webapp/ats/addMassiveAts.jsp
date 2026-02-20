<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>ATS Admin Panel</title>
    <style>
        body {
            margin: 0;
            background-color: #dcdcdc;
        }

        div {
            font-size: 20px;
        }

        h1 {
            text-align: center;
            color: white;
            background-color: black;
            padding: 0;
            margin: 0;
        }

        table {
            text-align: center;
        }

        th {
            color: white;
            background-color: black;
        }

        .active {
            color: green;
            text-decoration: underline;
        }

        .inactive {
            color: red;
            text-decoration: underline;
        }
    </style>
</head>
<body>
<h1>ATS Admin Panel</h1>
<h2>Add new/Edit Ats</h2>
<hr>
<form action="listAts.jsp">
    <input type="submit" value="Back">
</form>
<hr>
<form>
    <label for="multiline">Enter accounts like strings "email;password;displayName" :</label><br>
    <textarea id="multiline" name="multiline" rows="10" cols="50"></textarea>
    <hr>
    <input type="button" value="Create" onclick="create()">
</form>

<script>
    function create() {
        const textarea = document.getElementById("multiline");
        const text = textarea.value;

        const mmc_6274_room_values = '[100, 200]';
        const mqc_6275_room_values = '[100, 200]';
        // Split the text into lines
        const lines = text.split("\n");

        // Parse each line into an object
        const accounts = lines.map(line => {
            const [username, password, mqNickName] = line.split(";");
            return { username, password, mqNickName };
        });

        // Reverse the accounts list
        accounts.reverse();

        for(const account of accounts) {

            const link = 'add.jsp?' +
                'username=' + encodeURIComponent(account.username) +
                '&password=' + encodeURIComponent(account.password) +
                '&mqNickName=' + encodeURIComponent(account.mqNickName) +
                '&bankId=6274&active=false&startTime=00%3A00&endTime=23%3A59%3A59.999999999&days=1,2,3,4,5,6,7&games=856,862,867'+
                '&bankIds=6274,6275&dsBR=1.0&maBR=1.0&sxBR=1.0&dsSR=1.0&maSR=1.0&sxSR=1.0' +
                '&values6274=' + encodeURIComponent(mmc_6274_room_values) +
                '&values6275=' + encodeURIComponent(mqc_6275_room_values) +
                '&botId=0';

            const xhr = new XMLHttpRequest();
            xhr.open('POST', link);
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            xhr.onload = function () {
                if (xhr.status === 200) {
                    alert(xhr.response);
                    window.location.reload();
                } else if (xhr.status !== 200) {
                    alert('Cannot add account to Ats system. ' + xhr.response);
                }
            };
            xhr.send();

            console.log(`Display Name: ${account.displayName}, Email: ${account.email}, Password: ${account.password}`);
        }
    }
</script>
</body>
</html>
