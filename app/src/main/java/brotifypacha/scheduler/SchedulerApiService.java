package brotifypacha.scheduler;
import brotifypacha.scheduler.data_models.*;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SchedulerApiService {

    @GET("api/users/")
    public Call<ResultModel> get_users(
            @Query("match_fields") String match_fields,
            @Query("return_fields") String return_fields,
            @Query("limit") Integer limit,
            @Query("skip") Integer skip
    );

    @POST("api/users/")
    public Call<ResultModel> create_user(
            @Query("username") String username,
            @Query("password") String password,
            @Query("firebase_id") String firebase_id
    );

    @GET("api/schedules/")
    public Call<ResultModel> get_schedules(
            @Query("match_fields") String match_fields,
            @Query("return_fields") String return_fields,
            @Query("limit") Integer limit,
            @Query("skip") Integer skip
    );

    @POST("api/schedules/")
    public Call<ResultModel> create_schedule(
            @Query("name") String name,
            @Query("alias") String alias,
            @Query("availability") String availability,
            @Query("schedule") String schedule,
            @Query("first_day") String first_day
    );
}
