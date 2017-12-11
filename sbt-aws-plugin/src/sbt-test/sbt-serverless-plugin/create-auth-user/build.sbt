
import com.github.dnvriend.sbt.aws.task.CognitoUserDetails

usersToCreate := List(
//  CognitoUserDetails("test-user-1", "test-password-1", "eu-west-1_x28EYOMHi", "3ukfqda2irnbckj1qifbqeb44t"),
  CognitoUserDetails("test-user-2", "test-password-2", "eu-west-1_x28EYOMHi", "3ukfqda2irnbckj1qifbqeb44t")
)