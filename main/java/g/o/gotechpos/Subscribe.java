package g.o.gotechpos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;

import java.util.UUID;


public class Subscribe extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.subscribe);
    new RavePayManager(Subscribe.this).setAmount(0)
            .setEmail("geraldaphiri@gmail.com")
            .setfName("Gerald")
            .setlName("Phiri")
            .setCountry("NG")
            .setCurrency("ZMW")
            //setNarration(narration)
            .setPublicKey("FLWPUBK-bde2c89cb96542b99c682129eea76674-X")
            .setEncryptionKey("246902470ae6cb4425a1c24a")
            .setTxRef(UUID.randomUUID().toString())
            //.acceptAccountPayments(true)
            //.acceptCardPayments(true)
            //.acceptMpesaPayments(true)
            //.acceptAchPayments(boolean)
            //.acceptGHMobileMoneyPayments(boolean)
            //.acceptUgMobileMoneyPayments(boolean)
            .acceptZmMobileMoneyPayments(true)
            //.acceptRwfMobileMoneyPayments(boolean)
            //.acceptUkPayments(boolean)
            //.acceptBankTransferPayments(true)
            //.acceptUssdPayments(boolean)
            //.acceptFrancMobileMoneyPayments(boolean)
            //.onStagingEnv(true)
            //.setMeta(List<Meta>)
            // .withTheme(styleId)
            //.isPreAuth(boolean)
            // .setSubAccounts(List<SubAccount>)
            .shouldDisplayFee(true)
            .showStagingLabel(true)
            .initialize();

  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    /*
     *  We advise you to do a further verification of transaction's details on your server to be
     *  sure everything checks out before providing service or goods.
     */
    if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
      String message = data.getStringExtra("response");
      if (resultCode == RavePayActivity.RESULT_SUCCESS) {
        Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
      }
      else if (resultCode == RavePayActivity.RESULT_ERROR) {
        Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
      }
      else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
        Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
      }
    }
    else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }


}